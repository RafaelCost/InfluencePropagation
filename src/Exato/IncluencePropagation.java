package Exato;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class IncluencePropagation {
	

	public ArrayList<ArrayList<Incentives>> incentives;
	int n; //number of the vertices
	int matrixAdj[][]; //graph
	double h[];
	double alpha;
	
	//variables
	public ArrayList<ArrayList<IloNumVar>> x;
	public IloNumVar[][] z;
	public IloNumVar[] y;
	IloCplex cplex;
	
	
	
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
				incentives = new ArrayList<ArrayList<Incentives>>();
				
				for(int i = 0; i < n; i++) {
					String[] line = reader.readLine().split(" ");
					incentives.add(new ArrayList<Incentives>());
					
					for(int j = 0; j < line.length; j++) {
						String[] auxLine = line[j].split("-");
						double cost = Double.parseDouble(auxLine[0]);
						double incent = Double.parseDouble(auxLine[1]);
						incentives.get(i).add(new Incentives(cost, incent));	
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
	
	public class LCC extends IloCplex.LazyConstraintCallback {

		@Override
		public void main() throws IloException {
			int [][] valueoOfZ = new int[n][n];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if(matrixAdj[i][j]!=0 && i!=j) {
						valueoOfZ[i][j] = (int) getValue(z[i][j]);
					}
				}
			}

			System.out.println("teste");

			ArrayList<IloRange> restricoes = makeCuts(valueoOfZ);
			if (restricoes.size() > 0) {
				for (IloRange r : restricoes) {
					System.out.println("restricao add" + r.toString());
					add(r, 0);
				}
			}

		}
	}

	public void model() throws IloException {
		cplex = new IloCplex();
		
		x = new ArrayList<ArrayList<IloNumVar>>();
		y = new IloNumVar[n];
		z = new IloNumVar[n][n];

		//created variables x
		for (int i = 0; i < n; i++) {
			x.add(new ArrayList<IloNumVar>());
			for(int p = 0; p <incentives.get(i).size(); p++) {
				x.get(i).add(cplex.boolVar( "x(" + i + "," + p + ")"));
			}
		}
		
		
		//created variables z
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if(matrixAdj[i][j]!=0 && i!=j) {
					z[i][j] = cplex.boolVar("z(" + i + "," + j + ")");
				}
			}
		}
		
		//created variables y
		for (int i = 0; i < n; i++) {
			y[i] = cplex.boolVar("y(" + i + ")");
		}
		
		
		
		//Constraints 1: Compare the sum of chosen incentives and the influence coming from neighbors on in going arcs to a node’s hurdle.
		for(int i=0; i<n; i++) {
			IloLinearNumExpr constraints1 = cplex.linearNumExpr();
			
			for(int p = 0; p <incentives.get(i).size(); p++) {
				constraints1.addTerm(incentives.get(i).get(p).incentive, x.get(i).get(p));
			}
			
			for(int j=0; j<n; j++) {
				if(matrixAdj[i][j] != 0 && i != j) {
					constraints1.addTerm(matrixAdj[i][j], z[i][j]);
				}	
			}
			
			constraints1.addTerm(-1*h[i], y[i]);
			cplex.addGe(constraints1, 1);
		}
		
		//Constraints 2: Ensure that exactly one incentive is given to each activated node.
		for(int i=0; i<n; i++) {
			IloLinearNumExpr constraints1 = cplex.linearNumExpr();
			
			for(int p = 0; p <incentives.get(i).size(); p++) {
				constraints1.addTerm(1, x.get(i).get(p));
			}
			
			constraints1.addTerm(-1,y[i]);
			cplex.addEq(constraints1, 0);//
		}
		
		//Constraints 3: Forcing constraints
		for(int i=0; i<n; i++) {			
			for(int j = 0; j <n; j++) {
				IloLinearNumExpr constraints1 = cplex.linearNumExpr();
				
				if(j!=i && matrixAdj[i][j] != 0) {
					constraints1.addTerm(1,z[i][j]);
					constraints1.addTerm(-1,y[i]);
					cplex.addLe(constraints1, 0);
				}
			}
		}
		
		//Constraints 4: Coverage constraint.
		IloLinearNumExpr constraints1 = cplex.linearNumExpr();
		for(int i=0; i<n; i++) {			
			constraints1.addTerm(1,y[i]);
		}
		cplex.addGe(constraints1, alpha*n);
		
	
				
		//objective function
		IloLinearNumExpr objective = cplex.linearNumExpr();
		for(int i=0; i<n; i++) {
			for(int p = 0; p <incentives.get(i).size(); p++) {
				objective.addTerm(incentives.get(i).get(p).cost, x.get(i).get(p));
			}
		}
				
		// chamando callback
		cplex.use(new LCC());
	
		cplex.addMinimize(objective);
		
		
		//created model.lp		
		cplex.exportModel("C:\\Users\\rafae\\Documents\\Facul\\TCC\\Codgos\\Exato\\model.lp");
		
	
		//solver the problem
		float duration = 0;
		
		ArrayList<ArrayList<Integer>> valueOfX = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < n; i++) {
			valueOfX.add(new ArrayList<Integer>());
		}
		
		int[] valueOfY = new int[n];
		int[][] valueOfZ = new int[n][n];
		
		float startTime = System.nanoTime();
		if (cplex.solve()) {
			float endTime = System.nanoTime();
			duration = (endTime - startTime);
			//update values of the variables
			for(int i=0; i<n; i++) {	
				for(int p=0; p<incentives.get(i).size(); p++) {
						valueOfX.get(i).add((int) cplex.getValue(x.get(i).get(p)));
				}
			}
			
			for(int i=0; i<n; i++) {
				for(int j=0; j<n; j++) {
					if(i!=j && matrixAdj[i][j] != 0) {
						valueOfZ[i][j] = (int)cplex.getValue(z[i][j]);
					}
				}
			}
			
			for(int i=0; i<n; i++) {
				valueOfY[i] = (int)cplex.getValue(y[i]);
			}			
		}else {
			System.out.println("error - solve");
		}	
		
		
		for(int i=0; i<n; i++) {	
			for(int p=0; p<valueOfX.get(i).size(); p++) {
				System.out.println("x [" + i + ","+ p +"]= "+ valueOfX.get(i).get(p));
			}
		}
		
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				System.out.println("z [" + i + ","+ j +"]= "+ valueOfZ[i][j]);						
			}
		}
		
		for(int i=0; i<n; i++) {
			System.out.println("y [" + i + "]= "+ valueOfY[i]);
		}
			
			
		//value of the objective function		
		System.out.println("value OF:"+ cplex.getObjValue());
		System.out.println("Tempo de execução:" + duration);
		
		cplex.end();
				
		
	
	}
	
	
	
	public ArrayList<IloRange> makeCuts(int[][] matrix) throws IloException {
		ArrayList<IloRange> cuts = new ArrayList<IloRange>();
		
		for (int i = 0; i < n; i++) {
			ArrayList<Integer> cicle = dfs(i);
			if(!cicle.isEmpty()) {
				for (int j = 0; j < cicle.size(); j++) {
					System.out.println(cicle.get(i));
				}
				
			}
			
			/*
			if(!cicles.isEmpty()) {
				for (int j = 0; j < cicles.size(); j++) {
					Stack c = cicles.get(j);
					System.out.println("O CICLO EM "+i+":");
					for (int k = 0; k < c.size(); k++) {
						IloLinearNumExpr constraints1 = cplex.linearNumExpr();
						for (int i2 = 0; i2 < c.size()-1; i2++) {
							constraints1.addTerm(1,z[(int) c.get(i2)][(int) c.get(i2)+1]);
						}
						constraints1.addTerm(1,z[(int) c.lastElement()][(int) c.firstElement()]);
						
						
						for (int i2 = 0; i2 < c.size(); i2++) {
							if((int) c.get(i2) != k) {
								constraints1.addTerm(-1,y[(int) c.get(i2)]);
							}
							
						}
							
						cuts.add(cplex.addLe(constraints1, 0));

					}
				}					
			}*/
		}
		return cuts;
	}
	
	public ArrayList<Integer> dfs(int v) {
		Stack<Integer> stack = new Stack<Integer>();
		ArrayList<Integer> cicle = new ArrayList<Integer>();
		boolean[] visited = new boolean[n];
		boolean[] in_stack = new boolean[n];
		
		for (int i = 0; i < n; i++) {
			visited[i] = false;
			in_stack[i] = false;
		}
		
		while(true) {
			boolean find = false;
			
			if(!visited[v]) {
				stack.push(v);
				visited[v] = true;
				in_stack[v] = true;
				
			}
			int auxJ = 0;
			for (int j = 0; j < matrixAdj.length; j++) {
				if(j!=v && j==0) {
					if(in_stack[j]) {
						
						int aux = stack.pop();
						while(aux!= j ) {
							cicle.add(aux);
							aux = stack.pop();
						}
						
						return cicle;						
					}else if(!visited[j]) {
						find = true;
						auxJ = j;
						break;
					}
				}			
			}
			
			if(!find) {
				in_stack[stack.lastElement()] = false;
				stack.pop();
				if(stack.empty()) {
					break;
				}
				v = stack.lastElement();
			}else {
				v = auxJ;
			}
		}
		return cicle;
		
	}
			
	public static void main(String[] args) {
		IncluencePropagation m = new IncluencePropagation();
		
		String matrix_arq = "C:\\Users\\rafae\\Documents\\Facul\\TCC\\Codgos\\Exato\\Instancias\\Graph.txt";
		String icent_arq = "C:\\Users\\rafae\\Documents\\Facul\\TCC\\Codgos\\Exato\\Instancias\\Incentives.txt";
		String thresh_arq = "C:\\Users\\rafae\\Documents\\Facul\\TCC\\Codgos\\Exato\\Instancias\\Thresholds.txt";
		m.upload_graph(thresh_arq, matrix_arq,icent_arq);
		try {
			m.model();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
		
		
		
	

}
