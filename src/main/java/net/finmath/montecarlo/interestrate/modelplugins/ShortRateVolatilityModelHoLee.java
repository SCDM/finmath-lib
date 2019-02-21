/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 24.01.2016
 */

package net.finmath.montecarlo.interestrate.modelplugins;

import java.util.Objects;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * @author Christian Fries
 */
public class ShortRateVolatilityModelHoLee implements ShortRateVolailityModelInterface {

	private final double volatility;

	private final TimeDiscretizationInterface timeDiscretization = new TimeDiscretization(0.0);

	public ShortRateVolatilityModelHoLee(double volatility) {
		super();
		this.volatility = volatility;
	}

	@Override
	public TimeDiscretizationInterface getTimeDiscretization() {
		return timeDiscretization;
	}

	@Override
	public double getVolatility(int timeIndex) {
		return volatility;
	}

	@Override
	public double getMeanReversion(int timeIndex) {
		return 0.0;
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
        final ShortRateVolatilityModelHoLee other = (ShortRateVolatilityModelHoLee) obj;
        if (Double.doubleToLongBits(this.volatility) != Double.doubleToLongBits(other.volatility)) {
            return false;
        }
        if (!Objects.equals(this.timeDiscretization, other.timeDiscretization)) {
            return false;
        }
        return true;
    }
}
