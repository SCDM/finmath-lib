/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 24.01.2016
 */

package net.finmath.montecarlo.interestrate.modelplugins;

import java.util.Arrays;
import java.util.Objects;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * @author Christian Fries
 */
public class ShortRateVolatilityModel implements ShortRateVolailityModelInterface {

	private TimeDiscretizationInterface timeDiscretization;
	private double[] volatility;
	private double[] meanReversion;

	public ShortRateVolatilityModel(TimeDiscretizationInterface timeDiscretization, double[] volatility, double[] meanReversion) {
		super();
		this.timeDiscretization = timeDiscretization;
		this.volatility = volatility;
		this.meanReversion = meanReversion;
	}

	@Override
	public TimeDiscretizationInterface getTimeDiscretization() {
		return timeDiscretization;
	}

	@Override
	public double getVolatility(int timeIndex) {
		return volatility[timeIndex];
	}

	@Override
	public double getMeanReversion(int timeIndex) {
		return meanReversion[timeIndex];
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
        final ShortRateVolatilityModel other = (ShortRateVolatilityModel) obj;
        if (!Objects.equals(this.timeDiscretization, other.timeDiscretization)) {
            return false;
        }
        if (!Arrays.equals(this.volatility, other.volatility)) {
            return false;
        }
        if (!Arrays.equals(this.meanReversion, other.meanReversion)) {
            return false;
        }
        return true;
    }
}
