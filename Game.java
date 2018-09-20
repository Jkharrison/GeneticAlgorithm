import java.util.Random;
import java.util.TreeMap;
import java.io.*;
// java game > stats.txt
class MetaParams
{
	int mutationRate;
	float averageDevation;
	int numOfBattles;
	int numOfMates;
	MetaParams()
	{
		Random r = new Random();
		mutationRate = r.nextInt();
		averageDevation = (float)r.nextGaussian();
		numOfBattles = r.nextInt(20);
		numOfMates = r.nextInt(15);
	}
	void evolveMeta()
	{
		Random r = new Random();
		mutationRate = r.nextInt();
		averageDevation = (float)r.nextGaussian();
		numOfBattles = r.nextInt(20);
		numOfMates = r.nextInt(15);
	}
	void setParams(MetaParams mp)
	{
		this.mutationRate = mp.mutationRate;
		this.averageDevation = mp.averageDevation;
		this.numOfBattles = mp.numOfBattles;
		this.numOfMates = mp.numOfMates;
	}
	int getRate()
	{
		return this.mutationRate;
	}
	float getDevation()
	{
		return this.averageDevation;
	}
	int getBattles()
	{
		return this.numOfBattles;
	}
	int getMates()
	{
		return this.numOfMates;
	}
}

class Game
{
	static double[] evolveWeights() throws Exception
	{
		// Meta-Parameters.
		TreeMap<Integer, Integer> fitness = new TreeMap<>();
		// TreeMap<Integer, Double[]> correct = new TreeMap<>();
		int mutationRate;
		// float averageDevation = .06f;
		int numOfBattles = 50;
		int mostFit = Integer.MIN_VALUE;
		int mostFitIndex = -1;
		// int numOfMates = 10;
		int winningBonus = 100;
		// Create a random initial population
		boolean neuralWins = false; // Check if evolved weights beat the reflex agent.
		Random r = new Random();
		Matrix population = new Matrix(100, 291);
		MetaParams[] params = new MetaParams[100];
		for(int i = 0; i < 100; i++)
		{
			params[i] = new MetaParams();
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				chromosome[j] = 0.03 * r.nextGaussian();
		}
		// Evolve the population
		// todo: YOUR CODE WILL START HERE.
		//       Please write some code to evolve this population.
		//       (For tournament selection, you will need to call Controller.doBattleNoGui(agent1, agent2).)
		int iterations = 0;
		while(!neuralWins || iterations < 500)
		{
			numOfBattles = params[r.nextInt(100)].getBattles();
			// Promote Diversity
			for(int i = 0; i < population.rows(); i++)
			{
				double[] chromosome = population.row(i);
				MetaParams current = params[i];
				mutationRate = current.getRate();
				if(mutationRate >= 60) // Probability that Chromosome should be mutated.
				{
					chromosome[r.nextInt(population.cols())] += r.nextGaussian() * current.getDevation();
				}
			}
			// Natural Selection
			for(int i = 0; i < numOfBattles; i++)
			{
				// Choose random number of battles to occur and choose the red and blue agents.
				int blue = r.nextInt(population.rows());
				int red = r.nextInt(population.rows());
				int lives = r.nextInt(100);
				if(Controller.doBattleNoGui(new NeuralAgent(population.row(blue)), new NeuralAgent(population.row(red))) == 1)
				{
					// Choosing if winner lives or dies.
					if(lives >= 30)
						population.removeRow(red);
					else
						population.removeRow(blue);
				}
				else if(Controller.doBattleNoGui(new NeuralAgent(population.row(blue)), new NeuralAgent(population.row(red))) == -1)
				{
					if(lives >= 30)
						population.removeRow(blue);
					else
						population.removeRow(red);
				}
			}
			// Replenish Population
			while(population.rows() < 100)
			{
				int parentIndex = r.nextInt(population.rows());
				double[] parent = population.row(parentIndex);
				int[] candidates = new int[params[parentIndex].getMates()];
				for(int i = 0; i < candidates.length; i++)
				{
					int candidateIndex = r.nextInt(population.rows());
					while(candidateIndex == parentIndex)
					{
						candidateIndex = r.nextInt(population.rows());
					}
					candidates[i] = candidateIndex;
				}
				int max = Integer.MIN_VALUE;
				int similarParent = -1;
				for(int i = 0; i < candidates.length; i++) // Finding most compatiable parent #2.
				{
					if(similar(parent, population.row(candidates[i])) > max)
					{
						max = similar(parent, population.row(candidates[i]));
						similarParent = candidates[i];
					}
				}
				if(similarParent == -1)
					similarParent = r.nextInt(population.rows());
				double[] daddy = population.row(similarParent);
				double[] child = population.newRow();
				params[population.rows()-1] = new MetaParams();
				params[population.rows()-1].setParams(params[parentIndex]);
				int crossOverPoint = r.nextInt(population.cols());
				for(int i = 0; i < crossOverPoint; i++)
				{
						child[i] = parent[i];
				}
				for(int i = crossOverPoint; i < population.cols(); i++)
				{
					child[i] = daddy[i];
				}
			}
			for(int i = 0; i < population.rows(); i++)
			{
				// Time stamp
				long startTime = System.currentTimeMillis();
				if(Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(population.row(i))) == -1)
				{
					// Time stamp to win. bonus if winning.
					long endTime = System.currentTimeMillis();
					int difference = (int)(endTime - startTime);
					// Possibly do 200 - difference if winning the game quicker or not.
					fitness.put(i, difference + winningBonus); // 100 for winningBonus.
					// System.out.println("Time to win: " + difference);
					// System.out.println("Iteration to win: " + iterations);
					neuralWins = true;
					//System.out.println(difference+winningBonus);
					//System.out.println("Evolution complete");
					return population.row(i);
				}
				else
				{
					long endTime = System.currentTimeMillis();
					int difference = (int)(endTime - startTime);
					fitness.put(i, difference);
					//System.out.println("Time to lose: " + difference);
					// Time stamp to lose. Delta time ms lose.
				}
			}
			//int mostFit = Integer.MIN_VALUE;
			mostFitIndex = -1;
			for(int i = 0; i < fitness.size(); i++)
			{
				int fitnessScale = fitness.get(i);
				if(fitnessScale > mostFit)
				{
					mostFit = fitnessScale;
					mostFitIndex = i;
				}
			}
			//System.out.println("Most fit population: " + mostFit + " at index: " + mostFitIndex + ", Iteration: " + iterations);
			System.out.println(mostFit);
			iterations++;
		}
		// Return an arbitrary member from the population
		return population.row(0);
	}
	static int similar(double[] parent, double[] selectedParent)
	{
		int count = 0;
		for(int i = 0; i < parent.length && i < selectedParent.length; i++)
		{
			if(parent[i] == selectedParent[i])
				count++;
		}
		return count;
	}
	public static void main(String[] args) throws Exception
	{
		//double[] w = evolveWeights();
		// for(int i = 0; i < w.length; i++)
		// {
		// 	//System.out.println(w[i]);
		// }
		double[] winningWeights = new double[291]; // This will change with the weight I get from a winning population.
		int index = 0;
		try
		{	
			File file = new File("weight.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while((line = bufferedReader.readLine()) != null)
			{
				winningWeights[index] = Double.parseDouble(line);
				index++;
			}
			fileReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		for(int i = 0; i < winningWeights.length; i++)
		{
			System.out.println(winningWeights[i]);
		}
		Controller.doBattle(new ReflexAgent(), new NeuralAgent(winningWeights));
	}
}