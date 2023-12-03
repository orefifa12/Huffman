import java.io.File;


public class HuffMainDecompress {
	private final static String UNHUFF_EXTENSION = ".uhf";

	private static String getDefaultFileName(String name) {
		if (name.endsWith(".hf")) {
			return name.substring(0,name.length()-3)+UNHUFF_EXTENSION;
		}
		return name + UNHUFF_EXTENSION;
	}

	public void decompress(BitInputStream in, BitOutputStream out) {
		int bits = in.readBits(BITS_PER_INT);
		if (bits != HUFF_TREE) {
			throw new HuffException("Invalid magic number: " + bits);
		}
		
		HuffNode root = readTree(in);
		HuffNode current = root;
		
		while (true) {
			int bit = in.readBits(1); // Read one bit at a time
	
			// TODO: Traverse the Huffman tree using the 'bit' variable
			// and update the 'current' node accordingly.
			// Write leaf values to the output file.
	
			// TODO: Check for PSEUDO_EOF and break out of the loop if found.
		}
	
		// Close the output file after decompression
		out.close();
	}
	
	// Helper method to read the Huffman tree
	private HuffNode readTree(BitInputStream in) {
        bit = in.readBits(1);
        if (bit == -1) throw exception
        if (bit == 0) {
                left = readTree(...)
                right = readTree(...)
                 return new HuffNode(0,0,left,right);
        }
        else {
            value = read BITS_PER_WORD+1 bits from input
            return new HuffNode(value,0,null,null);
        }
  }

	

	public static void main(String[] args) {
		
		System.out.println("Huffman Decompress Main");
		System.out.println("Use FileDialog to choose file to decompress");
		File inf = FileSelector.selectFile("file to decompress");
		if (inf == null) {
			System.err.println("input file cancelled");
			return;
		}
		System.out.println("Use FileDialog to choose file name/directory for decompressed file");
		String saveName = getDefaultFileName(inf.getName());
		File outf = FileSelector.saveFile("decompressed file name",saveName);
		if (outf == null) {
			System.err.println("output file cancelled");
			return;
		}
		BitInputStream bis = new BitInputStream(inf);
		BitOutputStream bos = new BitOutputStream(outf);

		try {
			HuffProcessor hp = new HuffProcessor();
			long before = System.nanoTime();
			hp.decompress(bis, bos);
			long after = System.nanoTime();
			System.out.printf("uncompress from %s to %s\n",
					inf.getName(), outf.getName());

			System.out.printf("file: %d bits to %d bits\n", inf.length() * 8, outf.length() * 8);
			System.out.printf("read %d bits, wrote %d bits\n",
					bis.bitsRead(), bos.bitsWritten());
			long diff = 8 * (outf.length() - inf.length());
			long diff2 = bos.bitsWritten() - bis.bitsRead();
			System.out.printf("%d compared to %d\n", diff, diff2);
			System.out.printf("Decompress took %d milliseconds", (after-before)/1000000);
		}
		catch (HuffException he) {
			boolean result = outf.delete();
			if (result) {
				System.err.printf("deleted file %s\n", outf.getName());
			}
			he.printStackTrace();

		}
	}
}
