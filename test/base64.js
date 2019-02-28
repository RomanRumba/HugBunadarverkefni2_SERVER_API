const fs = require('fs');
const { promisify } = require('util');

const asyncReadFile = promisify(fs.readFile);
const asyncWriteFile = promisify(fs.writeFile);

async function base64Encode(file) {
    const bm = await asyncReadFile(file);
    return Buffer.from(bm).toString('base64');
}

async function base64Decode(base64str, file) {
    const bm = Buffer.from(base64str, 'base64');
    await asyncWriteFile(file, bm);
}


async function main() {

    const args = process.argv.slice(2);

    if (args.length < 3) {
        console.log('First argument must be `e` to encode file to base64');
        console.log('or `d` to decode.');
        console.log('The second argument must be the path to the input file');
        console.log('And the 3rd argument must be the path to the output file');
        return;
    }

    const [ type, inputFile, outputFile ] = args;

    if (!(type === 'e' || type === 'd')) {
        console.log('First argument _must_ be `e` or `d`');
        return;
    }

    

    if (type === 'e') {
        const base64file = await base64Encode(inputFile);
        await asyncWriteFile(outputFile, base64file);
    } else if (type === 'd') {
        const base64file = await asyncReadFile(inputFile);
        await base64Decode(base64file, outputFile);
    } else {
        console.error('failed!');
        return;
    }
}


main().catch((reason) => {
    console.error(reason);
});