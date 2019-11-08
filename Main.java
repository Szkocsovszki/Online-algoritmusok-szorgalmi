import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import net.sf.javailp.SolverFactoryGLPK;

public class Main {
	
	private static int m;
	private static ArrayList<Integer> input = new ArrayList<Integer>();
	private static LinkedList<Integer[]> orders = new LinkedList<Integer[]>();
	
	private static void read() {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Kérem a gépek számát!");
		m = sc.nextInt();
		
		System.out.println("Kérem az input sorozatot!");
		System.out.println("A sorozatot -1-gyel zárja!");
		int num;
		while ((num = sc.nextInt()) != -1) {
			input.add(num);
		}
		
		sc.close();
	}
	
	private static void variaton(int[] n, int[] Nr, int idx) {
	    if (idx == n.length) {
	    	Integer[] array = new Integer[n.length];
	    	for (int i = 0; i < n.length; i++) {
	    		array[i] = n[i];
	    	}
	    	orders.add(array);
	        return;
	    }
	    for (int i = 0; i <= Nr[idx]; i++) { 
	        n[idx] = i;
	        variaton(n, Nr, idx+1);
	    }
	}
	
	private static int[] initArray(int length) {
		int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = 0;
		}
		return array;
	}
	
	private static int min(int[] array) {
		int min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}
	
	private static int max(int[] array) {
		int max = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}
	
	private static int putOnMachines(int[] order, int[] jobs) {
		int[] machines = initArray(m);
		for (int i = 0; i < order.length; i++) {
			machines[order[i]] += jobs[i];
		}
		return max(machines);
	}
	
	public static void main(String[] args) {
		read();
		
		int length = input.size();
		int[] n = new int[length];
		int[] Nr = new int[length];
		for (int i = 0; i < length; i++) {
			Nr[i] = m - 1;
		}
		variaton(n, Nr, 0);
		
		int[] optimums = new int[input.size()];
		int index = 0;
		for (int i = 0; i < input.size(); i++) {
			int[] jobs = new int[i + 1];
			for (int j = 0; j <= i; j++) {
				jobs[j] = input.get(j);
			}
			
			int[] costs = new int[orders.size()];
			int costIndex = 0;
			for (Integer[] j : orders) {
				int[] order = new int[i + 1];
				for (int k = 0; k <= i; k++) {
					order[k] = j[k];
				}
				costs[costIndex++] = putOnMachines(order, jobs);
			}
			optimums[index++] = min(costs);
		}
				
		// http://javailp.sourceforge.net/
		SolverFactory factory = new SolverFactoryGLPK();
		factory.setParameter(Solver.VERBOSE, 0);
		factory.setParameter(Solver.TIMEOUT, 100);
		
		Problem problem = new Problem();
		
		// max t
		Linear linear = new Linear();
		linear.add(1, "t");
		problem.setObjective(linear, OptType.MAX);
		
		// pi >= 0
		for (int i = 0; i < input.size(); i++) {
			linear = new Linear();
			String variable = "p" + i;
			linear.add(1, variable);
			problem.add(linear, ">=", 0);
		}
		
		// sum pi = 1
		linear = new Linear();
		for (int i = 0; i < input.size(); i++) {
			String variable = "p" + i;
			linear.add(1, variable);
		}
		problem.add(linear, "=", 1);
		
		for (Integer[] order : orders) {
			linear = new Linear();
			for (int i = 0; i < input.size(); i++) {
				String variable = "p" + i;
				int[] orderPart = new int[i + 1];
				int[] jobs = new int[i + 1];
				for (int j = 0; j < orderPart.length; j++) {
					orderPart[j] = order[j];
					jobs[j] = input.get(j);
				}
				double coefficient = (double)putOnMachines(orderPart, jobs) / (double)optimums[i];
				linear.add(coefficient, variable);
			}
			linear.add(-1, "t");
			problem.add(linear, ">=", 0);
		}
		
		for (int i = 0; i < input.size(); i++) {
			String variable = "p" + i;
			problem.setVarType(variable, Double.class);
		}
		problem.setVarType("t", Double.class);
		
		Solver solver = factory.get();
		Result result = solver.solve(problem);

		System.out.println(result);
	}

}
