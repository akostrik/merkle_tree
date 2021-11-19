import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

import org.apache.commons.codec.DecoderException;

import ove.crypto.digest.Blake2b;

public class MerkleTree implements Serializable {
	MerkleTree left  = null;
	MerkleTree right = null;
	byte[]     hash  = null;
	byte[] 	   wantedHash = null;
	
	//////////// constructors
	public MerkleTree(ArrayList<String> donnees) throws IOException { // Ex 2
		if(donnees.size()==1) {
			this.left   = null;
			this.right  = null;
			this.hash   = Utils.blake2b.digest(donnees.get(0).getBytes()); // Utils.toBytes ?
		}
		else { // size=2^n
			ArrayList<String> partieGauche = new ArrayList<String>();
			partieGauche.addAll(donnees.subList(0,donnees.size()/2)); 
			ArrayList<String> partieDroite = new ArrayList<String>();
			partieDroite.addAll(donnees.subList(donnees.size()/2,donnees.size())); 
			this.left  = new MerkleTree(partieGauche);
			this.right = new MerkleTree(partieDroite);
			this.hash  = Utils.concat_hash(this.left.getHash(),this.right.getHash());
		}
	}
	
	public MerkleTree(MerkleTree left, MerkleTree right, byte[] hash) throws IOException {
		this.left  = left;
		this.right = right;
		this.hash  = hash;
	}
	
	public MerkleTree(byte[] hash) throws IOException, DecoderException { 
		this.left   = null;
		this.right  = null;
		this.hash   = hash; // Arrays.copy?
	}

	public MerkleTree(String str) throws IOException { 
		this.left   = null;
		this.right  = null;
		this.hash   = Utils.blake2b.digest(str.getBytes());
	}

	
	////////////// essential
	public byte[] witness(String str,MerkleTree tree) throws IOException {
		return witness(Utils.blake2b.digest(str.getBytes()),tree);
	}

	public byte[] witness(byte[] wantedHash, MerkleTree tree) throws IOException { 
		if(this.left==null || this.right==null) { 
			// the leaves (of the witness) have been already verified from theirs parent nodes // mais on ne rentre pas dans les feuilles ?
			return wantedHash;
		}
		else if(Arrays.equals(this.left.hash,wantedHash)) {
			// left child == wantedHash
			tree.right.left=null;
			tree.right.right=null;
			return Utils.concat_hash(this.left.hash, this.right.hash);
		}
		else if(Arrays.equals(this.right.hash,wantedHash)) {
			// right child == wantedHash
			tree.left.left=null;
			tree.left.right=null;
			return Utils.concat_hash(this.left.hash, this.right.hash);
		}
		else if(this.left.left==null || this.left.right==null || this.right.left==null || this.right.right==null) { 
			// is a parent of a leaf, no child == wantedHash
			tree.left=null;
			tree.right=null;
			return wantedHash;
		}
		else { 
			// isn't a leaf, isn't a parent of a leaf
			byte[] potentialNewWantedHashLeft  = this.left.witness(wantedHash,tree.left);
			if(!Arrays.equals(potentialNewWantedHashLeft,wantedHash)) {
				tree.right.left=null;
				tree.right.right=null;
				return potentialNewWantedHashLeft;
			}
			byte[] potentialNewWantedHashRight = this.right.witness(wantedHash,tree.right);
			if(!Arrays.equals(potentialNewWantedHashRight,wantedHash)) {
				tree.left.left=null;
				tree.left.right=null;
				return potentialNewWantedHashRight;
			}
		}
		return wantedHash; // null?
	}

	
	////////////////////// getters and utils
	public byte[] getHash() {
		return hash;
	}

	public String toString() {
		String rootHash = String.format("%.4s...",Utils.toHexString(this.hash));
		return rootHash + (this.left==null&&this.right==null ? "*  ":("  "+this.left.toString()+this.right.toString()));
	}
	
	public MerkleTree clone() {
		MerkleTree cloneTree = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ous = new ObjectOutputStream(baos);
			ous.writeObject(this);
			ous.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			cloneTree = (MerkleTree)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return cloneTree;
	}
			

	////////// archive
	public MerkleTree find(String wantedHash) throws DecoderException {
		return find(Utils.toBytesArray(wantedHash));
	}
		
	public MerkleTree find(byte[] wantedHash) {
		if(this.left==null || this.right==null) { // a leaf
			if(Arrays.equals(this.hash,wantedHash)) // compare?
				return this;
			else
				return null;
		}
		else {
			MerkleTree findLeftFree  = this.left .find(wantedHash);
			MerkleTree findRightFree = this.right.find(wantedHash);
			if(findLeftFree!=null)
				return findLeftFree;
			else if(findRightFree!=null)
				return findRightFree;
			else
				return null;
		}
	}

	public byte[] witnessAsListNodes(String str) throws IOException { // Ex 4
		return witnessAsListNodes(Utils.blake2b.digest(str.getBytes()));
	}
	
	public byte[] witnessAsListNodes(byte[] wantedHash) throws IOException { // Ex 4
		if(this.left==null || this.right==null) { 
			// the leaves (of the witness) have been already verified from theirs parent nodes // mais on ne rentre pas dans les feuilles ?
			return wantedHash;
		}
		else if(Arrays.equals(this.left.hash,wantedHash) || Arrays.equals(this.right.hash,wantedHash)) {
			// one of the children == wantedHash
			System.out.printf("%.4s\n",Utils.toHexString(this.left.hash)); ///
			System.out.printf("%.4s\n",Utils.toHexString(this.right.hash)); ///
			System.out.printf("%.4s\n",Utils.toHexString(this.hash));
			return Utils.concat_hash(this.left.hash, this.right.hash);
		}
		else if(this.left.left==null || this.left.right==null || this.right.left==null || this.right.right==null) { 
			// is a parent of a leaf, no child == wantedHash
			return wantedHash;
		}
		else { 
			// isn't leaf, isn't a parent of a leaf
			byte[] potentialNewWantedHashLeft  = this.left.witnessAsListNodes(wantedHash);
			if(!Arrays.equals(potentialNewWantedHashLeft,wantedHash)) {
				System.out.printf("%.4s\n",Utils.toHexString(this.hash));
				System.out.printf("%.4s\n",Utils.toHexString(this.right.hash));
				return potentialNewWantedHashLeft;
			}
			byte[] potentialNewWantedHashRight = this.right.witnessAsListNodes(wantedHash);
			if(!Arrays.equals(potentialNewWantedHashRight,wantedHash)) {
				System.out.printf("%.4s\n",Utils.toHexString(this.hash));
				System.out.printf("%.4s\n",Utils.toHexString(this.left.hash));
				return potentialNewWantedHashRight;
			}
		}
		return wantedHash; // null?
	}

	public byte[] pathToWantedHash(String str) throws IOException { 
		return pathToWantedHash(Utils.blake2b.digest(str.getBytes()));
	}
	
	public byte[] pathToWantedHash(byte[] wantedHash) throws IOException { 
		if(this.left==null || this.right==null) { 
			// the leaves (of the witness) have been already verified from theirs parent nodes // mais on ne rentre pas dans les feuilles ?
			return wantedHash;
		}
		else if(Arrays.equals(this.left.hash,wantedHash) || Arrays.equals(this.right.hash,wantedHash)) {
			// one of the children == wantedHash
			System.out.printf("%.4s\n",Utils.toHexString(this.left.hash));
			System.out.printf("%.4s\n",Utils.toHexString(this.hash));
			return Utils.concat_hash(this.left.hash, this.right.hash);
		}
		else if(this.left.left==null || this.left.right==null || this.right.left==null || this.right.right==null) { 
			// is a parent of a leaf, no child == wantedHash
			return wantedHash;
		}
		else { 
			// isn't leaf, isn't a parent of a leaf
			byte[] potentialNewWantedHashLeft  = this.left.pathToWantedHash(wantedHash);
			if(!Arrays.equals(potentialNewWantedHashLeft,wantedHash)) {
				System.out.printf("%.4s\n",Utils.toHexString(this.hash));
				return potentialNewWantedHashLeft;
			}
			byte[] potentialNewWantedHashRight = this.right.pathToWantedHash(wantedHash);
			if(!Arrays.equals(potentialNewWantedHashRight,wantedHash)) {
				System.out.printf("%.4s\n",Utils.toHexString(this.hash));
				return potentialNewWantedHashRight;
			}
		}
		return wantedHash; // null ?
	}

	public MerkleTree witness0(byte[] wantedHash) throws IOException, DecoderException { // Ex 4
		// ne marche pas
		if(this.left==null || this.left.left==null || this.left.right==null) {
			// there is no little children on the left
			System.out.printf("%.4s on the left  : no little children\n",Utils.toHexString(this.hash));
		}
		else if(Arrays.equals(this.left.left.hash,wantedHash) || Arrays.equals(this.left.right.hash,wantedHash)) {
			// a little child on the left == wantedHash
			System.out.printf("%.4s on the left  : a little child == wantedHash\n",Utils.toHexString(this.hash));
			this.right.left=null;
			this.right.right=null;
			System.out.printf("%.4s: %s\n",Utils.toHexString(this.hash),this);
		}
		else {
			// on the left : to verify
			System.out.printf("%.4s on the left  : to verify\n",Utils.toHexString(this.hash));
			byte[] hashLeftChildBefore = this.left.hash;
			System.out.printf("%.4s hashLeftChildBefore=%.4s\n",Utils.toHexString(this.hash),Utils.toHexString(hashLeftChildBefore));
			this.left = this.left.witness0(wantedHash);
			byte[] hashLeftChildAfter  = this.left.hash;
			System.out.printf("%.4s hashLeftChildAfter=%.4s\n",Utils.toHexString(this.hash),Utils.toHexString(hashLeftChildAfter));
			//if(Arrays.equals(hashLeftChildBefore,hashLeftChildAfter)) {
			//	this.left.left=null;
			//	this.left.right=null;
			//	System.out.printf("%.4s %s\n",Utils.toHexString(this.hash),this);
			//}
		}

		if(this.right==null || this.right.left==null || this.right.right==null) {
			// on the right : there is no little children 
			System.out.printf("%.4s on the right : no little children\n",Utils.toHexString(this.hash));
		}
		else if(Arrays.equals(this.right.left.hash,wantedHash) || Arrays.equals(this.right.right.hash,wantedHash)) {
			// on the right : a little child == wantedHash
			System.out.printf("%.4s on the right : a little child == wantedHash\n",Utils.toHexString(this.hash));
			this.left.left=null;
			this.left.right=null;
			System.out.printf("%.4s: %s\n",Utils.toHexString(this.hash),this);
		}
		else {
			// on the right: to verify
			System.out.printf("%.4s on the right : to verify\n",Utils.toHexString(this.hash));
			byte[] hashRightChildBefore = this.right.hash;
			this.right = this.right.witness0(wantedHash);
			System.out.printf("%.4s %s\n",Utils.toHexString(this.hash),this);
			byte[] hashRightChildAfter = this.right.hash;
			// if(!Arrays.equals(hashRightChildBefore,hashRightChildAfter)) {
			// 	this.right.left=null;
			//	this.right.right=null;
			//	System.out.printf("%.4s %s\n",Utils.toHexString(this.hash),this);
			//}				
			System.out.printf("%.4s %s\n",Utils.toHexString(this.hash),this);
			}
		System.out.printf("%.4s return %s\n",Utils.toHexString(this.hash),this);
		return this;		
	}
}