package ben.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by benh on 7/2/15.
 */
public class LocalSearch<T> {

    private final Objective<T> objective;
    private final NeighborhoodIterator<T> neighborhood;

    public LocalSearch(final Objective<T> objective,
                       final NeighborhoodIterator<T> neighborhood) {
        this.objective = objective;
        this.neighborhood = neighborhood;
    }

    public Solution<T> search(final T initial){ // steepest ascent...
        T best = initial;
        Solution<T> solution = new Solution<>(best, objective.eval(best));
        System.out.println("Initial objective: " + solution.objectiveValue);

        Solution<T> bestNeighbor = searchNeighborhood(solution);
        while (solution.objectiveValue < bestNeighbor.objectiveValue){
            solution = bestNeighbor;
            bestNeighbor = searchNeighborhood(solution);
        }
        System.out.println("Local optima objective: " + solution.objectiveValue);
        return solution;
    }

    private Solution<T> searchNeighborhood(final Solution<T> source){
        Solution<T> bestNeighbor = source;
        final Iterator<T> i = neighborhood.iterator(source.solution);
        while(i.hasNext()){
            final T neighbor = i.next();
            final double neighborObjective = objective.eval(neighbor);
            if (bestNeighbor.objectiveValue < neighborObjective){
                bestNeighbor = new Solution<>(neighbor, neighborObjective);
            }
        }
        return bestNeighbor;
    }

    public static class Solution<T> {
        public final T solution;
        public final double objectiveValue;
        public Solution(final T solution, final double objectiveValue){
            this.solution = solution;
            this.objectiveValue = objectiveValue;
        }
    }

    public static interface Objective<T> {
        public double eval(final T t);
    }

    public static interface NeighborhoodIterator<T> {
        public Iterator<T> iterator(final T t);
    }

    public static interface Strategy {
        public boolean accept(final double objective);
        public boolean terminate(final double objective);
    }

    // linear combination of objectives
    public static class LinearObjective<T> implements Objective<T> {
        private final Map<Objective<T>, Double> terms = new HashMap<>();
        public void putObjective(final Objective<T> objective, final Double coefficient){
            terms.put(objective, coefficient);
        }
        @Override public double eval(final T t){
            double result = 0.0;
            for(final Map.Entry<Objective<T>, Double> term: terms.entrySet()){
                final Objective<T> objective = term.getKey();
                final double coefficient = term.getValue();
                result += objective.eval(t) * coefficient;
            }
            return result;
        }
    }

}
