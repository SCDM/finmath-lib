/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 02.02.2017
 */

package net.finmath.montecarlo.interestrate.modelplugins;

import java.util.Arrays;
import java.util.Objects;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * @author Christian Fries
 *
 */
public class TermStructureTenorTimeScalingPicewiseConstant implements TermStructureTenorTimeScalingInterface {

	private final TimeDiscretizationInterface timeDiscretization;
	private final double timesIntegrated[];

	private final double floor = 0.1-1.0, cap = 10.0-1.0;
	private final double parameterScaling = 100.0;


	public TermStructureTenorTimeScalingPicewiseConstant(TimeDiscretizationInterface timeDiscretization, double[] parameters) {
		super();
		this.timeDiscretization = timeDiscretization;
		timesIntegrated = new double[timeDiscretization.getNumberOfTimes()];
		for(int timeIntervallIndex=0; timeIntervallIndex<timeDiscretization.getNumberOfTimeSteps(); timeIntervallIndex++) {
			timesIntegrated[timeIntervallIndex+1] = timesIntegrated[timeIntervallIndex] + (1.0+Math.min(Math.max(parameterScaling*parameters[timeIntervallIndex],floor),cap)) * (timeDiscretization.getTimeStep(timeIntervallIndex));
		}
	}

	@Override
	public double getScaledTenorTime(double periodStart, double periodEnd) {

		int timeStartIndex = timeDiscretization.getTimeIndexNearestLessOrEqual(periodStart);
		int timeEndIndex = timeDiscretization.getTimeIndexNearestLessOrEqual(periodEnd);

		if(timeDiscretization.getTime(timeStartIndex) != periodStart) System.out.println("*****S" + (periodStart));
		if(timeDiscretization.getTime(timeEndIndex) != periodEnd) System.out.println("*****E" + (periodStart));
		double timeScaled = timesIntegrated[timeEndIndex] - timesIntegrated[timeStartIndex];

		return timeScaled;
	}

	@Override
	public TermStructureTenorTimeScalingInterface getCloneWithModifiedParameters(double[] parameters) {
		return new TermStructureTenorTimeScalingPicewiseConstant(timeDiscretization, parameters);
	}

	/* (non-Javadoc)
	 * @see net.finmath.montecarlo.interestrate.modelplugins.TermStructureTenorTimeScalingInterface#getParameter()
	 */
	@Override
	public double[] getParameter() {
		double[] parameter = new double[timeDiscretization.getNumberOfTimeSteps()];
		for(int timeIntervallIndex=0; timeIntervallIndex<timeDiscretization.getNumberOfTimeSteps(); timeIntervallIndex++) {
			parameter[timeIntervallIndex] = ((timesIntegrated[timeIntervallIndex+1] - timesIntegrated[timeIntervallIndex]) / timeDiscretization.getTimeStep(timeIntervallIndex) - 1.0) / parameterScaling;
		}

		return parameter;
	}

	@Override
	public TermStructureTenorTimeScalingInterface clone() {
		return this;
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
        final TermStructureTenorTimeScalingPicewiseConstant other = (TermStructureTenorTimeScalingPicewiseConstant) obj;
        if (Double.doubleToLongBits(this.floor) != Double.doubleToLongBits(other.floor)) {
            return false;
        }
        if (Double.doubleToLongBits(this.cap) != Double.doubleToLongBits(other.cap)) {
            return false;
        }
        if (Double.doubleToLongBits(this.parameterScaling) != Double.doubleToLongBits(other.parameterScaling)) {
            return false;
        }
        if (!Objects.equals(this.timeDiscretization, other.timeDiscretization)) {
            return false;
        }
        if (!Arrays.equals(this.timesIntegrated, other.timesIntegrated)) {
            return false;
        }
        return true;
    }
}
