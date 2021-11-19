import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.codec.DecoderException;

public class main {
	public static void main(String[] args) throws IOException, DecoderException {

		ArrayList<String> donnees = new ArrayList<String>(); // size = 2^n
		//ArrayList<String> donnees = new ArrayList<String>(new String[]("a","b","c","d","e","f","g","h"));
		donnees.add("a");
		donnees.add("b");
		donnees.add("c");
		donnees.add("d");
		donnees.add("e");
		donnees.add("f");
		donnees.add("g");
		donnees.add("h");
		MerkleTree tree = new MerkleTree(donnees);
		System.out.println("INITIAL TREE : "+tree);
		
		MerkleTree witness= tree.clone();
		tree.witness("a",witness);
		System.out.println("WINTESS      : "+witness);
		
		//System.out.println(tree+"\n\n");
		//MerkleTree witness = treeClone.witness(Utils.toBytesArray("8D234302AEB06F2A7EFFB905E43F037E4DCA1C2A0F050E82175328C5CE8D31F4"));
		//MerkleTree witness = treeClone.witness("e");
		//System.out.println("WITNESS = "+witness.toString());
	}
}