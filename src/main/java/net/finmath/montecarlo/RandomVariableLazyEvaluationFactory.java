/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 09.02.2004
 */
package net.finmath.montecarlo;

import net.finmath.stochastic.RandomVariableInterface;

/**
 *
 * @author Christian Fries
 */
public class RandomVariableLazyEvaluationFactory extends AbstractRandomVariableFactory {

    @Override
    public RandomVariableInterface createRandomVariable(double time, double value) {
        return new RandomVariableLazyEvaluation(time, value);
    }

    @Override
    public RandomVariableInterface createRandomVariable(double time, double[] values) {
        return new RandomVariableLazyEvaluation(time, values);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RandomVariableLazyEvaluationFactory other = (RandomVariableLazyEvaluationFactory) obj;
        return true;
    }
}
