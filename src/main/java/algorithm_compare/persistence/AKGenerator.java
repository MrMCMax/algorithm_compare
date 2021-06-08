package algorithm_compare.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class AKGenerator {
	
	private static PrintWriter pw;
	private static int vertices;
	
	public static void main(String[] args) {
		if (args.length == 1) {
			openPrintWriter(args[0]);
			System.out.println("How many vertices in the upper path:");
			Scanner s = new Scanner(System.in);
			vertices = s.nextInt();
			s.close();
		} else if (args.length == 2) {
			openPrintWriter(args[1]);
			try {
				vertices = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Usage: AKGenerator nVertices outFile");
				System.exit(0);
			}
		} else {
			System.err.println("Usage: AKGenerator nVertices outFile");
			System.exit(0);
		}
		if (vertices < 1) {
			System.err.println("At least input one vertex. Try again");
			System.exit(0);
		}
		printAKNetwork();
	}
	
	private static void openPrintWriter(String file) {
		try {
			pw = new PrintWriter(new File(PersistenceService.PATH_TO_RESOURCES + file));
			//pw = new PrintWriter(System.out);
		} catch (FileNotFoundException ex) {
			System.err.println("Bad file name, try again");
			System.exit(1);
		}
	}
	
	/**
	 * Prints an AK network with the static fields of vertices and printwriter.
	 */
	private static void printAKNetwork() {
		int n = vertices * 2 + 2;
		int m = 3*vertices + 2;
		int s = n-1;
		int t = n;
		pw.println("c AK Network with " + vertices + " vertices in the upper path");
		pw.println("p max    " + n + "    " + m);
		pw.println("n    " + s + "  s");
		pw.println("n    " + t + "  t");
		//Now the edges
		//The vertices in the upper path are 1..vertices
		//The vertices in the lower path are vertices+1..vertices+n
		for (int i = 1; i < vertices; i++) {
			pw.println("a    " + i + "    " + (i + 1) + "    " + (vertices - i + 1));
		}
		pw.flush();
		for (int i = vertices + 1; i < 2*vertices; i++) {
			pw.println("a    " + i + "    " + (i + 1) + "    " + (vertices + 1));
		}
		pw.flush();
		for (int i = 1; i <= vertices; i++) {
			pw.println("a    " + i + "    " + (vertices + 1) + "    " + 1);
		}
		pw.println("a    " + s + "    " + 1 + "    " + (vertices + 1));
		pw.println("a    " + s + "    " + (vertices + 1) + "    " + 1);
		pw.println("a    " + vertices + "    " + t + "    " + 1);
		pw.println("a    " + (2*vertices) + "    " + t + "    " + (vertices + 1));
		pw.flush();
		pw.close();
	}
}
