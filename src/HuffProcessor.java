import java.util.PriorityQueue;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 *
 * Revise
 */

public class HuffProcessor {

	private class HuffNode implements Comparable<HuffNode> {
		HuffNode left;
		HuffNode right;
		int value;
		int weight;

		public HuffNode(int val, int count) {
			value = val;
			weight = count;
		}
		public HuffNode(int val, int count, HuffNode ltree, HuffNode rtree) {
			value = val;
			weight = count;
			left = ltree;
			right = rtree;
		}

		public int compareTo(HuffNode o) {
			return weight - o.weight;
		}
	}

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private boolean myDebugging = true;
	
	public HuffProcessor() {
		this(true);
	}
	
	public HuffProcessor(boolean debug) {
		myDebugging = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){

		int[] counts = getCounts(in); // freq of every 8-bit
	  	HuffNode root = makeTree(counts) ; //creates new encodings greedily
	  	in.reset(); //reset
	  	out.writeBits(BITS_PER_INT ,HUFF_TREE);//write magic num
	  	writeTree(root,out);//make the new tree
	  	String[] encodings = new String[ALPH_SIZE+1]; // Encodings for each car
	  	makeEncodings(root, "", encodings); // make the encoding for each 8bit char
		in.reset(); //reset
		while(true){
			int bits = in.readBits(BITS_PER_WORD);//reads the bit for the next word
			if(bits == -1) // There are no more bits to read
				break;
			String code = encodings[bits];
    		out.writeBits(code.length(), Integer.parseInt(code,2));
		}
		String code = encodings[PSEUDO_EOF];
		out.writeBits(code.length(), Integer.parseInt(code,2));	
		out.close();
	}

	private int[] getCounts(BitInputStream in)
	{
		int[] count = new int[ALPH_SIZE];
		while(true)
		{
			int bits = in.readBits(BITS_PER_WORD);
			if(bits == -1)
				break;
			count[bits] += 1;
		}
		return count;
	}

	private HuffNode makeTree(int[] counts){
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();
		for(int i = 0; i < counts.length; i++) {
			if (counts[i] > 0){
				pq.add(new HuffNode(i,counts[i],null,null));
			}
		}
		pq.add(new HuffNode(PSEUDO_EOF,1,null,null)); // account for PSEUDO_EOF having a single occurrence

		while (pq.size() > 1) {
		HuffNode left = pq.remove();
		HuffNode right = pq.remove();
		HuffNode t = new HuffNode(0, pq.size(), left, right);
		// create new HuffNode t with weight from
		// left.weight+right.weight and left, right subtrees
		pq.add(t);
		}
		HuffNode root = pq.remove();
		return root;
	}

	private void writeTree(HuffNode root, BitOutputStream out)
	{
		if(root == null)
			return;
		if(checkLeaf(root))
		{
			out.writeBits(1, 1);
			out.writeBits(BITS_PER_WORD + 1, root.value);
			return;
		}
		out.writeBits(1, 0);
		writeTree(root.left, out);
		writeTree(root.right, out);
	}

	private void makeEncodings(HuffNode root, String path,String[] encodings){
		if (checkLeaf(root)) {
			encodings[root.value] = path;
			return;
		   }
		else{
			makeEncodings(root.left, path + "0", encodings);
			makeEncodings(root.right, path + "1", encodings);
		}
	}
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){

		int bits = in.readBits(BITS_PER_INT);
		if(bits != HUFF_TREE)
		{
			throw new HuffException("invalid magic number" + bits);
		}
		HuffNode root = readTree(in);
		HuffNode current = root;
		while(true)
		{
			bits = in.readBits(1);
			if(bits == -1)
			{
				throw new HuffException("bad input, no PSEUDO_EOF");
			}
			else
			{
				if(bits == 0)
					current = current.left;
				else
					current = current.right;
				
				if(checkLeaf(current))
				{
					if(current.value == PSEUDO_EOF)
						break;
					else
					{
						out.writeBits(BITS_PER_WORD, current.value);
						current = root;
					}
				}
			}
		}
		out.close(); //close output file
	}

	private boolean checkLeaf(HuffNode t)
	{
		if(t == null)
			return false;
		if(t.left == null && t.right == null)
			return true;
		return false;
	}

	private HuffNode readTree(BitInputStream in) {
        int bit = in.readBits(1);
        if (bit == -1) throw new HuffException("Incorrect Bit");
        if (bit == 0) {
                HuffNode left = readTree(in);
                HuffNode right = readTree(in);
                return new HuffNode(0,0,left,right);
        }
        else {
            int value = in.readBits(BITS_PER_WORD+1);
            return new HuffNode(value,0,null,null);
        }
  	}


}