import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.Comparator;



class Game
{

	static double[] evolveWeights() throws Exception
	{
		// Meta-Parameters.
		TreeMap<Integer, Integer> fitness = new TreeMap<>();
		float mutationRate;
		float averageDevation = .06f;
		int numOfBattles = 50;
		int numOfMates = 0;
		int winningBonus = 100;
		// Create a random initial population
		boolean neuralWins = false; // Check if evolved weights beat the reflex agent.
		Random r = new Random();
		Matrix population = new Matrix(100, 291);
		for(int i = 0; i < 100; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				chromosome[j] = 0.03 * r.nextGaussian();
		}
		// Evolve the population
		// todo: YOUR CODE WILL START HERE.
		//       Please write some code to evolve this population.
		//       (For tournament selection, you will need to call Controller.doBattleNoGui(agent1, agent2).)
		int iterations = 0;
		while(!neuralWins || iterations < 1000)
		{
			// Promote Diversity
			for(int i = 0; i < population.rows(); i++)
			{
				double[] chromosome = population.row(i);
				mutationRate = r.nextInt(100);
				if(mutationRate >= 60) // Probability that Chromosome should be mutated.
				{
					chromosome[r.nextInt(population.cols())] += r.nextGaussian() * averageDevation;
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
					if(lives >= 40)
						population.removeRow(red);
					else
						population.removeRow(blue);
				}
				else
				{
					if(lives >= 40)
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
				int[] candidates = new int[5];
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
				double[] daddy = population.row(similarParent);
				double[] child = population.newRow();//new double[parent.length];
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
					fitness.put(i, difference + winningBonus); // 100 for winningBonus.
					System.out.println("Time to win: " + difference);
					neuralWins = true;
					// Controller.doBattle(new ReflexAgent(), new NeuralAgent(population.row(i)));
					System.out.println("Evolution complete");
					return population.row(i);
					// return population.row(i);
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
			int mostFit = Integer.MIN_VALUE;
			int mostFitIndex = -1;
			for(int i = 0; i < population.rows(); i++)
			{
				int fitnessScale = fitness.get(i);
				if(fitnessScale > mostFit)
				{
					mostFit = fitnessScale;
					mostFitIndex = i;
				}
			}
			System.out.println("Most fit population: " + mostFit + " at index: " + mostFitIndex);
			// System.out.println(fitness);
			iterations++;
		}


		// Return an arbitrary member from the population
		return population.row(20);
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
		double[] w = evolveWeights();
		Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
	}

}
