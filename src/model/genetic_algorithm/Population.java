package model.genetic_algorithm;

import model.genetic_algorithm.Chromosome;
import model.traffic.Sequence;
import model.traffic.TrafficLogicController;

import java.util.*;

public class Population{
    private List<Chromosome> population;
    private int populationSize;
    /**
     * Konstruktori i klasës Population.
     * Krijon një popullsi të re me madhësinë e caktuar.
     *
     * @param populationSize Madhësia e popullsisë.
     */
    public Population(int populationSize) {
        this.populationSize = populationSize;
        this.population = new ArrayList<>();
    }

    public Chromosome performGeneticAlgorithm(){
        Chromosome randomSequence = new Chromosome(new Sequence(TrafficLogicController.getTrafficLights(), new boolean[TrafficLogicController.getTrafficLights().length][TrafficLogicController.getSequenceLength()]));

        PriorityQueue<Chromosome> P = initializePopulation(randomSequence, this.populationSize);
        Chromosome best = P.peek();
        population = new ArrayList<>(P);

        int k = 1;
        int times = TrafficLogicController.getAllowedTimeToRun();
        while (best.getScore() != 1_000_000 && k < times){
            PriorityQueue<Chromosome> Q = new PriorityQueue<>(populationSize, new ChromosomeComparator());

            Q = performCrossover(0.5);
            Q = performMutation(0.1, Q);

            Q.addAll(P);
            P = performSelection(Q);

            best = P.peek();
            population = new ArrayList<>(P);

            System.out.println("Iterimi " + k + ": " + best.getScore());
            k += 15;
        }
        return best;
    }
    /**
     * Inicializimi i popullsisë.
     * Krijon një popullsi fillestare të përbërë nga kromozome të rastësishme.
     *
     * @param sequence              Sekuenca e dritave të trafikut për të krijuar kromozome të rastësishme.
     * @param initialPopulationSize Madhësia e popullsisë fillestare.
     */
    public PriorityQueue<Chromosome> initializePopulation(Sequence sequence, int initialPopulationSize) {
        PriorityQueue<Chromosome> P = new PriorityQueue<>(populationSize, new ChromosomeComparator());
        for (int i = 0; i < initialPopulationSize; i++) {
            Chromosome chromosome = createRandomChromosome(sequence);
            P.add(chromosome);
        }
        return P;
    }
    /**
     * Krijimi i kromozomit të rastësishëm.
     * Krijon një kromozom të rastësishëm duke marrë sekuencën fillestare dhe vendos gjenet e kromozomit
     * në mënyrë të rastësishme.
     *
     * @param sequence Sekuenca fillestare e dritave të trafikut.
     * @return Kromozomi i rastësishëm.
     */
    private Chromosome createRandomChromosome(Sequence sequence) {
        boolean[][] originalSequence = sequence.getSequence();
        boolean[][] randomSequence = new boolean[originalSequence.length][];
        for (int i = 0; i < originalSequence.length; i++) {
            randomSequence[i] = originalSequence[i].clone();
            for (int j = 0; j < originalSequence[i].length; j++) {
                randomSequence[i][j] = Math.random() < 0.5; // Set gene randomly to true or false
            }
        }
        return new Chromosome(new Sequence(sequence.getTrafficLights(), randomSequence));
    }
    /**
     * Zgjedhja e një individit të rastësishëm.
     * Zgjedh një individ të rastësishëm nga popullsia aktuale.
     *
     * @return Individi i zgjedhur.
     */
    public Chromosome selectIndividual() {
        Random random = new Random();
        int randomIndex = random.nextInt(population.size());
        return population.get(randomIndex);
    }
    /**
     * Realizimi i crossover-it (krijuarit të re) në popullsi.
     * Realizon crossover (krijuarit të re) midis kromozomeve të popullsisë.
     * Crossover ndodh me një shans prej crossoverRate, duke prodhuar të dy fëmijët e ri.
     *
     * @param crossoverRate Shansi i crossover-it për çiftin e kromozomeve.
     */
    public PriorityQueue<Chromosome> performCrossover(double crossoverRate) {
        PriorityQueue<Chromosome> offspring = new PriorityQueue<>(populationSize, new ChromosomeComparator());
        while (offspring.size() < populationSize) {
            Chromosome parent1 = selectIndividual();
            Chromosome parent2 = selectIndividual();
            if (Math.random() < crossoverRate) {
                Chromosome[] children = parent1.crossover(parent2);
                offspring.add(children[0]);
                offspring.add(children[1]);
            } else {
                offspring.add(parent1);
                offspring.add(parent2);
            }
        }
        return offspring;
    }


    /**
     * @param selectionElements krijohet gjenerata e re nga crossover dhe me pas thirret perfromSelection duke ja pasuar
     *                  listen me kromozome offspring
     *                  offspring sortohet me ane te f-onit getScore si Comparator ne PriorityQueue
     *                  prej qasaj PQ vendosen me ni AL edhe pastaj merren
     *                  90% e popullates prej better half
     *                  10% e popullates prej other half
     *                  edhe corssover e merr si popullata e re
     * @return
     */
    public PriorityQueue<Chromosome> performSelection(PriorityQueue<Chromosome> selectionElements){
        List<Chromosome> selectedList = new ArrayList<>();
        while(selectionElements.size()>0){
            selectedList.add(selectionElements.poll());
        }
        List<Chromosome> finalSelectedList = new ArrayList<>();
        for (int i = 0; i < (int)(populationSize*0.9); i++) {
            finalSelectedList.add(selectedList.get(getRandomNumber(0,(selectedList.size()/2))));
        }
        for (int i = 0; i < (int)(populationSize*0.1); i++) {
            finalSelectedList.add(selectedList.get(getRandomNumber((selectedList.size()/2), selectedList.size())));
        }

        PriorityQueue<Chromosome> f = new PriorityQueue<>(populationSize, new ChromosomeComparator());
        for (int i = 0; i < populationSize; i++) {
            f.add(finalSelectedList.get(i));
        }
        return f;
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    class ChromosomeComparator implements Comparator<Chromosome>{
        @Override
        public int compare(Chromosome ch1, Chromosome ch2) {
            if(ch1.getScore() < ch2.getScore()){
                return 1;
            }
            else if (ch1.getScore() > ch2.getScore()) {
                return -1;
            }
            return 0;
        }
    }

    /**
     * Realizimi i mutacionit në popullsi.
     * Realizon mutacion në secilin kromozom të popullsisë.
     * Mutacion ndodh me një shans prej mutationRate.
     *
     * @param mutationRate Shansi i mutacionit për secilin kromozom.
     */
    public PriorityQueue<Chromosome> performMutation(double mutationRate, PriorityQueue<Chromosome> q) {
        List<Chromosome> p = new ArrayList<>(q);
        for (int i = 0; i < p.size(); i++) {
            Chromosome chromosome = p.get(i);
            if (Math.random() < mutationRate) {
                try{
                    Chromosome mutatedChromosome = chromosome.mutate();
                    p.remove(chromosome);
                    p.add(mutatedChromosome);
                }catch (Exception ignored){}

                //population.set(population.indexOf(chromosome), mutatedChromosome);
            }
        }
        return q;
    }
    /**
     * Merr popullsinë aktuale.
     *
     * @return Popullsia aktuale.
     */
    public List<Chromosome> getPopulation() {
        return population;
    }
    /**
     * Vendos popullsinë e re.
     *
     * @param population Popullsia e re.
     */
    public void setPopulation(List<Chromosome> population) {
        this.population = population;
    }
    /**
     * Merr madhësinë e popullsisë.
     *
     * @return Madhësia e popullsisë.
     */
    public int getPopulationSize() {
        return populationSize;
    }
    /**
     * Vendos madhësinë e popullsisë.
     *
     * @param populationSize Madhësia e re e popullsisë.
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public static void main(String[] args) {
        Population p = new Population(20);

        p.performGeneticAlgorithm();
    }
}
