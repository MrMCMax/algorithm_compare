package algorithm_compare.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import algorithm_compare.logic.ILogicService;
import algorithm_compare.logic.LogicService;

public class ConsoleInterface {

	static ILogicService logicService;
	static Scanner s;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 0)
			interactiveRun();
		else if (args.length == 3) {
			try {
				int option = Integer.parseInt(args[0]);
				try {
					logicService = new LogicService();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (option == 1) {
					runAlgorithms(args[1], args[2]);
				} else if (option == 2) {
					getTimes(args[1], args[2]);
				} else {
					showUsage();
				}
			} catch (NumberFormatException e) {
				showUsage();
			}
		} else {
			showUsage();
		}
	}

	static void showUsage() {
		System.out.println("Usage: ConsoleInterface option netList algList");
		System.out.println("Where option can be 1 - run algorithms or 2 - view stats");
	}

	static void interactiveRun() {
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
		long[] flowResults = logicService.computeNetworkWithAlgorithms(network, new String[] { algorithm });
		System.out.println(Arrays.toString(flowResults));
	}

	static void runAlgorithms(String netLine, String algLine) {
		String[] algorithms = algLine.split(", ");
		String[] networks = netLine.split(", ");
		try {
			for (int i = 0; i < networks.length; i++) {
				long[] flowResults = logicService.computeNetworkWithAlgorithms(networks[i], algorithms);
				System.out.println(Arrays.toString(flowResults));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void getTimes() throws IOException {
		System.out.println("Algorithms available:");
		List<String> algorithms = logicService.getListOfAlgorithms();
		for (String s : algorithms) {
			System.out.println(s);
		}
		System.out.println("Select algorithms: emtpy line to finish");
		String algorithm;
		List<String> selectedAlgorithms = new LinkedList<>();
		do {
			algorithm = s.nextLine();
			if (!algorithm.equals(""))
				selectedAlgorithms.add(algorithm);
		} while (!algorithm.equals(""));
		System.out.println("Available networks:");
		List<String> networks = logicService.getListOfNetworks();
		for (String s : networks) {
			System.out.println(s);
		}
		System.out.println("Select networks: empty line to finish");
		String network;
		List<String> selectedNetworks = new LinkedList<>();
		do {
			network = s.nextLine();
			if (!network.equals(""))
				selectedNetworks.add(network);
		} while (!network.equals(""));
		Long[][] results = new Long[selectedNetworks.size()][selectedAlgorithms.size()];
		int netIx = 0;
		for (String net : selectedNetworks) {
			long[] timeResults = logicService.retrieveTimes(net, selectedAlgorithms.toArray(new String[0]));
			for (int i = 0; i < timeResults.length; i++) {
				results[netIx][i] = timeResults[i];
			}
			netIx++;
		}
		System.out.println(Plots.pythonText(logicService, selectedNetworks, selectedAlgorithms, results));
	}

	static void getTimes(String netLine, String algLine) {
		String[] networks = netLine.split(", ");
		String[] algorithms = algLine.split(", ");
		Long[][] results = new Long[networks.length][algorithms.length];
		int netIx = 0;
		try {
			for (String net : networks) {
				long[] timeResults = logicService.retrieveTimes(net, algorithms);
				for (int i = 0; i < timeResults.length; i++) {
					results[netIx][i] = timeResults[i];
				}
				netIx++;
			}
			System.out.println(
					Plots.pythonText(logicService, Arrays.asList(networks), Arrays.asList(algorithms), results));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
