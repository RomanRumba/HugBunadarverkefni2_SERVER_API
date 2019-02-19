package project.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import project.errors.NotFoundException;
import project.persistance.entities.Chatroom;
import project.services.ChatroomService;
import project.services.UserService;

@RestController
@RequestMapping("/auth/res")
public class FileUploadController {
	
	@Autowired
	private ChatroomService chatroomService;

	@Autowired
	private UserService userService;
	
	@Value("${content.directory}")
	private String fileDirectory;
	
	

	@RequestMapping(value = "/{chatroomName}/{hash}", method = RequestMethod.GET)
	public void download(@PathVariable String chatroomName, @PathVariable("hash") String hash, HttpServletResponse response) throws IOException, NotFoundException {
		// 9884cc96c8cf88f60e61058503f9fd9654223bdc1a092a8b1d2d1c12c09daea6
		
		// 64 characters
		// extract filename part
		
        Chatroom chatroom = chatroomService.findByChatname(chatroomName);
        
        System.out.println("Get from: " + chatroomName);
		
		// TODO: ensure hash is legal
		String path = Paths.get(fileDirectory, hash).toString();
		
		File file = new File(path);
		
		if (file.exists()) {
			
			InputStream is = new FileInputStream(file);
		    // copy it to response's OutputStream
			IOUtils.copy(is, response.getOutputStream());
		     response.flushBuffer();
		} else {
			// SOMEHOW INDICATE IT FAILED
			response.sendError(404, "Resource not found");
			response.flushBuffer();			
		}
	}
	

	
	// @RequestMapping("/upload")
	// @RequestMapping("/download ")
    // @RequestMapping("/upload")
	@RequestMapping(path = "/{chatroomName}", method = RequestMethod.POST)
    public String upload(@PathVariable String chatroomName, HttpServletRequest request) throws IOException, FileUploadException, NoSuchAlgorithmException, NotFoundException {

    	// TODO: I think it's possible to send multiple files, which I don't want.
        
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(request);
        
        Chatroom chatroom = chatroomService.findByChatname(chatroomName);
        
        System.out.println("Posting to: " + chatroomName);
        
        JSONObject obj = new JSONObject();

        
        while (iterator.hasNext()) {
            FileItemStream item = iterator.next();
            

            if (!item.isFormField()) {
            	
                final String filename = item.getName();
                final String contentType = item.getContentType();
                
                System.out.println("Filename: " + filename);
                System.out.println("Content type: " + contentType);
            	
                InputStream inputStream = item.openStream();
                String hash = handleInputStream(inputStream);
                
                obj.put("sha512", hash);
                obj.put("filename", filename);
                obj.put("content-type", contentType);
                
                // TODO: save metadata
                //...
            }
        }
		return obj.toString();
    }

	private String toHex(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (byte b : bytes) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}
	

	private String getRandomName(int n) {
		Random r = new Random();

		StringBuffer sb = new StringBuffer();

		String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
		for (int i = 0; i < n; i++) {
			sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
		}

		return sb.toString();
	}
	
	private String handleInputStream(InputStream dataStream) throws NoSuchAlgorithmException, IOException {
		
		final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		
		// TODO: ensure temporaryName doesn't exist

		String temporaryName = getRandomName(64);
		String pathToSaveFile = Paths.get(fileDirectory, temporaryName).toString();

		File targetFile = new File(pathToSaveFile);

		OutputStream outStream = new FileOutputStream(targetFile);

		byte[] buffer = new byte[8 * 1024];
		int bytesRead;
		while ((bytesRead = dataStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
			messageDigest.update(buffer, 0, bytesRead);
		}
		IOUtils.closeQuietly(dataStream);
		IOUtils.closeQuietly(outStream);

		byte[] hash = messageDigest.digest();
		
		String hashDigest = toHex(hash);
		
		
		String finalPathToSave = Paths.get(fileDirectory, hashDigest).toString();
		
		// Rename file
		
		// File (or directory) with old name
		File file = new File(pathToSaveFile);

		// File (or directory) with new name
		File file2 = new File(finalPathToSave);
		
		if (file2.exists()) {
			file.delete();
		} else {
			file.renameTo(file2);
		}
		
		return hashDigest;
	}
}
