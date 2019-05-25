package Heuristico;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import Heuristico.Incentives;
import Heuristico.InfluencePropagation;
import ilog.concert.IloException;

public class InfluencePropagation {

	public Stack<Incentives> incentives;
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
						incentives.add(new Incentives(totalIncentives, cost, incent, j+1));	
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
			System.out.println(incentives.get(i).id+"-Custo: "+incentives.get(i).cost+"  -  Incentivo: "+incentives.get(i).incentive+"  -  N�: "+incentives.get(i).node);
		}
        System.out.println();

	}
	
	public void solverFirst() {
		
		while(totalActiveNodes < (n*alpha)) {	
			Incentives incent = incentives.pop();
			int node = incent.node;
			
			
		
		
		}
		
	}
	
	public static void main(String[] args) {
		InfluencePropagation m = new InfluencePropagation();
		
		String matrix_arq = "C:\\Users\\rafae\\eclipse-workspace\\InfluencePropagation\\src\\Instances\\Graph.txt";
		String icent_arq = "C:\\Users\\rafae\\eclipse-workspace\\InfluencePropagation\\src\\Instances\\Incentives.txt";
		String thresh_arq = "C:\\Users\\rafae\\eclipse-workspace\\InfluencePropagation\\src\\Instances\\Thresholds.txt";
		m.upload_graph(thresh_arq, matrix_arq,icent_arq);
		m.start_Structs();
	
	}

}