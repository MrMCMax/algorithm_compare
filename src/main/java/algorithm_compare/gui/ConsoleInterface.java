package algorithm_compare.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import algorithm_compare.logic.ILogicService;
import algorithm_compare.logic.LogicService;

public class ConsoleInterface {

	static ILogicService logicService;
	static Scanner s;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			logicService = new LogicService();
			s = new Scanner(System.in);
			int task = 0;
			do {
				System.out.println("Select task:");
				System.out.println("0 - exit");
				System.out.println("1 - run algorithms");
				System.out.println("2 - view runtime results");
				System.out.println("Input task:");
				boolean success = true;
				do {
					try {
						task = s.nextInt();
						s.nextLine();
					} catch (NumberFormatException e) {
						System.err.println("Please select the index of a valid task (number)");
						success = false;
					}
				} while (!success);
				
				if (task == 1) {
					runAlgorithm();
				} else if (task == 2) {
					getTimes();
				}
			} while (task != 0);
		} catch (IOException e) {
			System.err.println("Couldn't start logic service: ");
			e.printStackTrace();
		} finally {
			s.close();
		}
	}
	
	static void runAlgorithm() throws IOException {
		System.out.println("Algorithms available:");
		List<String> algorithms = logicService.getListOfAlgorithms();
		for (String s : algorithms) {
			System.out.println(s);
		}
		System.out.println("Select an algorithm:");
		String algorithm = s.nextLine();
		System.out.println("Available networks:");
		List<String> networks = logicService.getListOfNetworks();
		for (String s : networks) {
			System.out.println(s);
		}
		System.out.println("Select a network:");
		String network = s.nextLine();
		long[] flowResults = logicService.computeNetworkWithAlgorithms(network, new String[] {algorithm});
		System.out.println(Arrays.toString(flowResults));
	}
	
	static void getTimes() throws IOException {
		System.out.println("Algorithms available:");
		List<String> algorithms = logicService.getListOfAlgorithms();
		for (String s : algorithms) {
			System.out.println(s);
		}
		System.out.println("Select an algorithm:");
		String algorithm = s.nextLine();
		System.out.println("Available networks:");
		List<String> networks = logicService.getListOfNetworks();
		for (String s : networks) {
			System.out.println(s);
		}
		System.out.println("Select a network:");
		String network = s.nextLine();
		long[] timeResults = logicService.retrieveTimes(network, new String[] {algorithm});
		System.out.println(Arrays.toString(timeResults));
	}

}
