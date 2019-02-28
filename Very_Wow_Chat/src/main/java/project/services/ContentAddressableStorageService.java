package project.services;

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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 *
 */
@Service
public class ContentAddressableStorageService {

	@Value("${content.directory}")
	private String fileDirectory;
	
	/**
	 * Returns true if successful, otherwise false
	 * 
	 * @param hash
	 * @param servletOutputStream
	 * @return
	 * @throws IOException 
	 */
	public boolean fetchAsServletOutputStream(String hash, ServletOutputStream servletOutputStream) throws IOException {
		// TODO: ensure hash is legal
		String path = Paths.get(fileDirectory, hash).toString();
		
		File file = new File(path);
		
		if (file.exists()) {
			
			InputStream is = new FileInputStream(file);
		    // copy it to response's OutputStream
			IOUtils.copy(is, servletOutputStream);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * 
	 * @param hash
	 * @param servletOutputStream
	 * @throws IOException 
	 */
	public void fetchAsHttpServletResponse(String hash, HttpServletResponse httpServletResponse) throws IOException {
		boolean success = fetchAsServletOutputStream(hash, httpServletResponse.getOutputStream());
		if (!success) {
			httpServletResponse.sendError(404, "Resource not found");
		}
		httpServletResponse.flushBuffer();
	}
	
	
	/**
	 * 
	 * @param bytes
	 * @return SHA-256 of bytes
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String storeBytes(byte[] bytes) throws NoSuchAlgorithmException, IOException {
		
		final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		
		messageDigest.update(bytes);

		byte[] hash = messageDigest.digest();
		
		String hashDigest = toHex(hash);
		String finalPath = Paths.get(fileDirectory, hashDigest).toString();
		
		File finalFile = new File(finalPath);
		
		if (!finalFile.exists()) {
			FileUtils.writeByteArrayToFile(finalFile, bytes);
		}
		
		return hashDigest;
	}
	
	/**
	 * 
	 * @param dataStream
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String storeInputStream(InputStream dataStream) throws NoSuchAlgorithmException, IOException {
		
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

	/**
	 * 
	 * @param bytes
	 * @return
	 */
	private String toHex(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (byte b : bytes) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}
	
	/**
	 * 
	 * @param n
	 * @return
	 */
	private String getRandomName(int n) {
		Random r = new Random();

		StringBuffer sb = new StringBuffer();

		String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
		for (int i = 0; i < n; i++) {
			sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
		}

		return sb.toString();
	}
}
