package Heuristico;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import Heuristico.Incentives;
import Heuristico.InfluencePropagation;
import ilog.concert.IloException;

public class InfluencePropagation {

	public Stack<Incentives> incentives;
	public ArrayList<Incentives> useIncentives;
	int n; //number of the vertices
	int matrixAdj[][]; //graph
	double h[];
	double alpha;
	
	int totalIncentives;
	int totalActiveNodes;
	boolean activeNodes[];
	double incentivesIn[];
	boolean incentivesUsed[];
	
	public void upload_graph(String arqThreshold, String arqMatrixAdj, String arqIncentives) {
		FileReader arq;
		BufferedReader reader;
		try {
			
			//Start Adj Matrix
			arq = new FileReader(arqMatrixAdj);
			reader = new BufferedReader(arq);
			try {	
				
				String line = reader.readLine();
				n = Integer.parseInt(line);
				matrixAdj = new int[n][n];
				
				for(int i = 0; i < n; i++) {
					line = reader.readLine();
					String[] aux_line = line.split(" ");
					for(int j = 0; j < n; j++) {
						matrixAdj[i][j] = Integer.parseInt(aux_line[j]);						
					}
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Start Thresholds values 
			arq = new FileReader(arqThreshold);
			reader = new BufferedReader(arq);
			try {	
				alpha = Double.parseDouble(reader.readLine());
				String[] line = reader.readLine().split(" ");

				h = new double[n];
				
				for(int i = 0; i < n; i++) {
					h[i] = Double.parseDouble(line[i]);
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//Start Incentives
			arq = new FileReader(arqIncentives);
			reader = new BufferedReader(arq);
			try {									
				incentives = new Stack<Incentives>();
				totalIncentives = 0;
				for(int i = 0; i < n; i++) {
					String[] line = reader.readLine().split(" ");
					
					for(int j = 0; j < line.length; j++) {
						String[] auxLine = line[j].split("-");
						double cost = Double.parseDouble(auxLine[0]);
						double incent = Double.parseDouble(auxLine[1]);
						incentives.add(new Incentives(totalIncentives, cost, incent, i));	
						totalIncentives++;
					}
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start_Structs(){
		
		totalActiveNodes = 0;
		
		activeNodes = new boolean[n];
		for (int i = 0; i < n; i++) {
			activeNodes[i] = false;
		}
		
		incentivesIn = new double[n];
		for (int i = 0; i < n; i++) {
			incentivesIn[i] = 0;
		}	
		
		incentivesUsed = new boolean[totalIncentives];
		for (int i = 0; i < totalIncentives; i++) {
			incentivesUsed[i] = false;
		}
		
        Collections.sort (incentives, (o1, o2) -> {
		    Incentives i1 = (Incentives) o1;
		    Incentives i2 = (Incentives) o2;
		    return i1.cost < i2.cost ? +1 : (i1.cost > i2.cost ? -1 : 0);
		});
        
        
        for (int i = 0; i < incentives.size(); i++) {
			System.out.println(incentives.get(i).id+"-Custo: "+incentives.get(i).cost+"  -  Incentivo: "+incentives.get(i).incentive+"  -  Nó: "+incentives.get(i).node);
		}
        System.out.println();
        
        useIncentives = new ArrayList<Incentives>();

	}
	
	public double firstSoluction() {
		
		
		System.out.println("N: "+n);
		System.out.println("A: "+alpha);
		System.out.println("|X|: "+(n*alpha));
		System.out.println();
		
		double totalCost = 0;
		
		Stack<Incentives> incentivesAux = new Stack<Incentives>();
		for (int i = 0; i < incentives.size(); i++) {
			incentivesAux.add(i, incentives.get(i));
		}
		
		while(totalActiveNodes < (n*alpha)) {	
			Incentives incent = incentivesAux.pop();
			int node = incent.node;
			
			//System.out.println("Select "+incent.id+" - Custo de: "+incent.cost+"    - Incentivo de: "+incent.incentive+ "   - No "+node);
			//System.out.println("Precisa de: "+h[node]);
			//System.out.println();
			
			if(!activeNodes[node]) {
				if(incentivesIn[node]+incent.incentive >= h[node]) {
					totalCost = totalCost + incent.cost;
					useIncentives.add(incent);
					incentivesUsed[incent.id] = true;					
					incentivesIn[node] = incentivesIn[node]+incent.incentive;
					
					propagationProcess(node);
					
				}	
			}
		}
		
		System.out.println("totalActiveNodes "+totalActiveNodes);
		for (int i = 0; i < useIncentives.size(); i++) {
			System.out.println("ID "+useIncentives.get(i).id+" - Custo de: "+useIncentives.get(i).cost+"    - Incentivo de: "+useIncentives.get(i).incentive+ "   - No "+useIncentives.get(i).node);
		}
		
		return totalCost;

	}
	
	public double calcBenefit(int node, double cost) {
		double outNode = 0;
		double inNode = 0;
		
		for (int i = 0; i < matrixAdj.length; i++) {
			outNode = matrixAdj[node][i];
			inNode = matrixAdj[i][node];
		}
		
		return((outNode - cost)/inNode);
	}
	
	public double newSoluction(ArrayList<Incentives> incentivesUseds, double prob) {
		//Resetar Estruturas

		totalActiveNodes = 0;
		
		activeNodes = new boolean[n];
		for (int i = 0; i < n; i++) {
			activeNodes[i] = false;
		}
		
		incentivesIn = new double[n];
		for (int i = 0; i < n; i++) {
			incentivesIn[i] = 0;
		}	
		
		incentivesUsed = new boolean[totalIncentives];
		for (int i = 0; i < totalIncentives; i++) {
			incentivesUsed[i] = false;
		}
		useIncentives = new ArrayList<Incentives>();
		Stack<Incentives> incentivesAux = new Stack<Incentives>();
		for (int i = 0; i < incentives.size(); i++) {
			incentivesAux.add(i, incentives.get(i));
		}
		//---------------------------------------
		boolean [] selected = new boolean[n];
		double totalCost = 0;
		
		for (int i = 0; i < n; i++) {
			selected[i] = false;
		}
		
		//System.out.println("ANTES -"+useIncentives);

		
		for (int i = 0; i < incentivesUseds.size(); i++) {
			int auxNode = incentivesUseds.get(i).node;
			double auxBenefit = calcBenefit(auxNode, incentivesUseds.get(i).cost); // Calcular o beneficio do no auxNode referente aos nós selecionados inialmente
			Incentives incent = incentivesUseds.get(i);
			
			for (int j = 0; j < matrixAdj.length; j++) { //verificar quais os nós q auxNode faz ligação
				
				if(j!=auxNode && matrixAdj[auxNode][j]!= 0 && !selected[j]) {
					double costAux = 0;
					Incentives incentAux = null;
					for (int z = 0; z < incentives.size(); z++) { //Encontrar o custo do incentivo isolado necessario do nó j
						if(incentives.get(z).node == j && incentives.get(z).incentive >= h[j]) {
							costAux = incentives.get(z).cost;
							incentAux = incentives.get(z);
						}
					}
					
					double auxBenefit2 = calcBenefit(j, incentivesUseds.get(i).cost);// Calc seu beneficio
					
					
						if(auxBenefit2 > auxBenefit && !incentivesUsed[incent.id]) { //fzr a substituição para um nó com maior custo beneficio
							
							auxBenefit = auxBenefit2;
							selected[j] = true;
							selected[auxNode] = false;
							auxNode = j;
							incent = incentAux;
	
						}else { // Depende da probabilidade para aceitar um nó com menor custo beneficio
							double auxProb = Math.random();
							if(auxProb <= prob && costAux != 0) {
								
								auxBenefit = auxBenefit2;
								selected[j] = true;
								selected[auxNode] = false;
								auxNode = j;
							}		
						}
					
				}
			}
			
			if(!incentivesUsed[incent.id]) {
				totalCost = totalCost + incent.cost;
				useIncentives.add(incent);
				incentivesUsed[incent.id] = true;					
				incentivesIn[incent.node] = incentivesIn[incent.node]+incent.incentive;
			}
		}
		
		
		for (int i = 0; i < useIncentives.size(); i++) {
			propagationProcess(useIncentives.get(i).node);
		}
		
		
		//Continuar com o processo de ativação dos nós 
		while(totalActiveNodes < (n*alpha)) {	
			Incentives incent = incentivesAux.pop();
			int node = incent.node;
			
			//System.out.println("Select "+incent.id+" - Custo de: "+incent.cost+"    - Incentivo de: "+incent.incentive+ "   - No "+node);
			//System.out.println("Precisa de: "+h[node]);
			//System.out.println();
			
			if(!activeNodes[node]) {
				if(incentivesIn[node]+incent.incentive >= h[node]) {
					
					totalCost = totalCost + incent.cost;
					useIncentives.add(incent);
					incentivesUsed[incent.id] = true;					
					incentivesIn[node] = incentivesIn[node]+incent.incentive;
					
					propagationProcess(node);
					
				}	
			}
		}
		
		return totalCost;
	}
	
	public void simulatedAnnealing() {
		
		double auxCost = firstSoluction();
		System.out.println("Custo inicial: "+auxCost);
	
		//Salvar status das estruturas	
		boolean[] activeNodesAux = new boolean[n];
		double[] incentivesInAux = new double[n];
		ArrayList<Incentives> incentivesAux = new ArrayList<Incentives>();
		boolean[] incentivesUsedAux = new boolean[totalIncentives];
		
		for (int i = 0; i < n; i++) {
			activeNodesAux[i] = activeNodes[i];
			incentivesInAux[i] = incentivesIn[i];
		}
		
		for (int i = 0; i < totalIncentives; i++) {
			incentivesUsedAux[i] = incentivesUsed[i];
		}
		for (int i = 0; i < useIncentives.size(); i++) {
			incentivesAux.add(useIncentives.get(i));
		}

		
		double prob = 0.2;
		double newCostAux = 0;
		System.out.println("");
		System.out.println("OLD SOLUTION");
		System.out.println("Cost-> "+auxCost);
		System.out.println("Incentives Used: ");
		for (int i = 0; i < incentivesAux.size(); i++) {
			System.out.println(" ID-> "+incentivesAux.get(i).id+" - Custo de: "+incentivesAux.get(i).cost+"    - Incentivo de: "+incentivesAux.get(i).incentive+ "   - No "+incentivesAux.get(i).node);
		
		}
		System.out.println("totalActiveNodes-> "+totalActiveNodes);

		
		for(int zzz=0; zzz<100; zzz++) {
			
			newCostAux = newSoluction(incentivesAux, prob);
			
			double auxProb = Math.random();
		
			if(newCostAux < auxCost ||auxProb <= prob ) {
				
				auxCost = newCostAux;
				incentivesAux = new ArrayList<Incentives>();	
				for (int i = 0; i < useIncentives.size(); i++) {
					incentivesAux.add(useIncentives.get(i));
				}
				prob = 0.0;
			}else {
				for (int i = 0; i < n; i++) {
					activeNodes[i] = activeNodesAux[i];
					incentivesIn[i]= incentivesInAux[i];
				}
				for (int i = 0; i < totalIncentives; i++) {
					incentivesUsed[i] = incentivesUsedAux[i];
				}
				
				prob = prob + 0.1;	
			}
		}
		System.out.println("");
		System.out.println("NEW SOLUTION");
		System.out.println("Cost-> "+auxCost);
		System.out.println("Incentives Used: ");
		for (int i = 0; i < incentivesAux.size(); i++) {
			System.out.println(" ID "+incentivesAux.get(i).id+" - Custo de: "+incentivesAux.get(i).cost+"    - Incentivo de: "+incentivesAux.get(i).incentive+ "   - No "+incentivesAux.get(i).node);
		}
		
		System.out.println("totalActiveNodes-> "+totalActiveNodes);
	}
	
	public void propagationProcess(int i) {
		if(incentivesIn[i] >= h[i]) {
			//System.out.println("Ativar nó"+i);
			totalActiveNodes++;
			activeNodes[i] = true;
		}
		
		if(activeNodes[i]) {
			for (int j = 0; j < matrixAdj.length; j++) {
				if(i!=j && matrixAdj[i][j] != 0 ) {
					incentivesIn[j] = incentivesIn[j] + matrixAdj[i][j];
					if(!activeNodes[j]) {
						propagationProcess(j);
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		InfluencePropagation m = new InfluencePropagation();
		
		String matrix_arq = "C:\\Users\\rafae\\eclipse-workspace\\InfluencePropagation\\src\\Instances\\Graph.txt";
		String icent_arq = "C:\\Users\\rafae\\eclipse-workspace\\InfluencePropagation\\src\\Instances\\Incentives.txt";
		String thresh_arq = "C:\\Users\\rafae\\eclipse-workspace\\InfluencePropagation\\src\\Instances\\Thresholds.txt";
		m.upload_graph(thresh_arq, matrix_arq,icent_arq);
		m.start_Structs();
		m.simulatedAnnealing();
	
	}

}
