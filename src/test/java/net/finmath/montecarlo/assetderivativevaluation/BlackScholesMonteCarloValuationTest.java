/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 19.01.2004, 21.12.2012
 */
package net.finmath.montecarlo.assetderivativevaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.AbstractRandomVariableFactory;
import net.finmath.montecarlo.BrownianMotionLazyInit;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.assetderivativevaluation.models.BlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AsianOption;
import net.finmath.montecarlo.assetderivativevaluation.products.BermudanOption;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.montecarlo.automaticdifferentiation.backward.RandomVariableDifferentiableAADFactory;
import net.finmath.montecarlo.automaticdifferentiation.forward.RandomVariableDifferentiableADFactory;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcessFromProcessModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


/**
 * This class represents a collection of several "tests" illustrating different aspects
 * related to the Monte-Carlo Simulation and derivative valuation (using a simple
 * Black-Scholes model.
 *
 * @author Christian Fries
 */
@RunWith(Parameterized.class)
public class BlackScholesMonteCarloValuationTest {

	@Parameters(name="{0}")
	public static Collection<Object[]> generateData()
	{
		return Arrays.asList(new Object[][] {
			{ new RandomVariableFactory(true /* isUseDoublePrecisionFloatingPointImplementation */) },
			{ new RandomVariableFactory(false /* isUseDoublePrecisionFloatingPointImplementation */) },
			{ new RandomVariableDifferentiableAADFactory() },
			{ new RandomVariableDifferentiableADFactory() },
		});
	}

	// Model properties
	private final double	initialValue   = 1.0;
	private final double	riskFreeRate   = 0.05;
	private final double	volatility     = 0.30;

	// Process discretization properties
	private final int		numberOfPaths		= 20000;
	private final int		numberOfTimeSteps	= 10;
	private final double	deltaT				= 0.5;

	private final int		seed				= 3141;

	private AssetModelMonteCarloSimulationModel model = null;
	private AbstractRandomVariableFactory randomVariableFactory = null;

	/**
	 * This main method will test a Monte-Carlo simulation of a Black-Scholes model and some valuations
	 * performed with this model.
	 *
	 * @param args Arguments - not used.
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 * @throws InterruptedException Thrown if multi-threadded execution is interrupted.
	 */
	public static void main(String[] args) throws CalculationException, InterruptedException
	{
		BlackScholesMonteCarloValuationTest pricingTest = new BlackScholesMonteCarloValuationTest(new RandomVariableFactory(true /* isUseDoublePrecisionFloatingPointImplementation */));

		/*
		 * Read input
		 */
		int testNumber = readTestNumber();

		long start = System.currentTimeMillis();

		switch(testNumber) {
		case 1:
			// This test requires a MonteCarloBlackScholesModel and will not work with others models
			pricingTest.testEuropeanCall();
			break;
		case 2:
			pricingTest.testModelProperties();
			break;
		case 3:
			pricingTest.testModelRandomVariable();
			break;
		case 4:
			pricingTest.testEuropeanAsianBermudanOption();
			break;
		case 5:
			pricingTest.testMultiThreaddedValuation();
			break;
		case 6:
			// This test requires a MonteCarloBlackScholesModel and will not work with others models
			pricingTest.testEuropeanCallDelta();
			break;
		case 7:
			// This test requires a MonteCarloBlackScholesModel and will not work with others models
			pricingTest.testEuropeanCallVega();
			break;
		}

		long end = System.currentTimeMillis();

		System.out.println("\nCalculation time required: " + (end-start)/1000.0 + " seconds.");
	}

	public BlackScholesMonteCarloValuationTest(AbstractRandomVariableFactory randomVariableFactory) {
		super();
		this.randomVariableFactory  = randomVariableFactory;
	}

	private static int readTestNumber() {
		System.out.println("Please select a test to run (click in this window and enter a number):");
		System.out.println("\t 1: Valuation of European call options (with different strikes).");
		System.out.println("\t 2: Some model properties.");
		System.out.println("\t 3: Print some realizations of the S(1).");
		System.out.println("\t 4: Valuation of European, Asian, Bermudan option.");
		System.out.println("\t 5: Multi-Threadded valuation of some ten thousand Asian options.");
		System.out.println("\t 6: Sensitivity (Delta) of European call options (with different strikes) using different methods.");
		System.out.println("\t 7: Sensitivity (Vega) of European call options (with different strikes) using different methods.");
		System.out.println();
		System.out.print("Test to run: ");

		//  open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		int testNumber = 0;
		try {
			String test = br.readLine();
			testNumber = Integer.valueOf(test);
		} catch (IOException ioe) {
			System.out.println("IO error trying to read test number!");
			System.exit(1);
		}

		System.out.println();
		return testNumber;
	}

	public AssetModelMonteCarloSimulationModel getModel()
	{
		/*
		 * Lazy initialize the model
		 */
		if(model == null) {
			// Create the time discretization
			TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(0.0, numberOfTimeSteps, deltaT);

			// Create the model
			ProcessModel blackScholesModel = new BlackScholesModel(initialValue, riskFreeRate, volatility);

			// Create a corresponding MC process
			MonteCarloProcessFromProcessModel process = new EulerSchemeFromProcessModel(new BrownianMotionLazyInit(timeDiscretization, 1 /* numberOfFactors */, numberOfPaths, seed, randomVariableFactory));

			model = new MonteCarloAssetModel(blackScholesModel, process);
		}

		return model;
	}

	/**
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 */
	@Test
	public void testEuropeanCall() throws CalculationException
	{
		/*
		 * Create the valuation model (see <code>getModel</code>)
		 */
		AssetModelMonteCarloSimulationModel model = getModel();

		// Java DecimalFormat for our output format
		DecimalFormat numberFormatStrike	= new DecimalFormat("     0.00 ");
		DecimalFormat numberFormatValue		= new DecimalFormat(" 0.00E00");
		DecimalFormat numberFormatDeviation	= new DecimalFormat("  0.00E00; -0.00E00");

		// Test options with different strike
		System.out.println("Valuation of European Options");
		System.out.println(" Strike \t Monte-Carlo \t Analytic \t Deviation");

		/*
		 * Cast the model to to get the parameters for analytic valuation
		 */
		double initialValue	= model.getAssetValue(0.0, 0).get(0);
		// @TODO This needs to be changes to use random variables.
		double riskFreeRate	= ((BlackScholesModel)((MonteCarloAssetModel)model).getModel()).getRiskFreeRate().getAverage();
		double volatility	= ((BlackScholesModel)((MonteCarloAssetModel)model).getModel()).getVolatility().getAverage();

		double optionMaturity	= 1.0;
		for(double optionStrike = 0.60; optionStrike < 1.50; optionStrike += 0.05) {

			// Create a product
			EuropeanOption		callOption	= new EuropeanOption(optionMaturity, optionStrike);
			// Value the product with Monte Carlo
			double valueMonteCarlo	= callOption.getValue(model);

			// Calculate the analytic value
			double valueAnalytic	= net.finmath.functions.AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, volatility, optionMaturity, optionStrike);

			// Print result
			System.out.println(numberFormatStrike.format(optionStrike) +
					"\t" + numberFormatValue.format(valueMonteCarlo) +
					"\t" + numberFormatValue.format(valueAnalytic) +
					"\t" + numberFormatDeviation.format(valueMonteCarlo-valueAnalytic));

			Assert.assertTrue(Math.abs(valueMonteCarlo-valueAnalytic) < 1E-02);
		}
	}

	/**
	 * Test some properties of the model
	 *
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 */
	@Test
	public void testModelProperties() throws CalculationException {
		/*
		 * Create the valuation model (see <code>getModel</code>)
		 */
		AssetModelMonteCarloSimulationModel model = getModel();

		TimeDiscretization modelTimeDiscretization = model.getTimeDiscretization();

		System.out.println("Time \tAverage \t\tVariance");
		for(double time : modelTimeDiscretization) {
			RandomVariable assetValue = model.getAssetValue(time, 0);

			double average	= assetValue.getAverage();
			double variance	= assetValue.getVariance();
			double error	= assetValue.getStandardError();

			DecimalFormat formater2Digits = new DecimalFormat("0.00");
			DecimalFormat formater4Digits = new DecimalFormat("0.0000");
			System.out.println(formater2Digits.format(time) + " \t" + formater4Digits.format(average) + "\t+/- " + formater4Digits.format(error) + "\t" + formater4Digits.format(variance));
		}
	}

	/**
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 */
	@Test
	public void testModelRandomVariable() throws CalculationException {
		/*
		 * Create the valuation model (see <code>getModel</code>)
		 */
		AssetModelMonteCarloSimulationModel model = getModel();

		RandomVariable stockAtTimeOne = model.getAssetValue(1.0, 0);

		System.out.println("The first 100 realizations of the " + stockAtTimeOne.size() + " realizations of S(1) are:");
		System.out.println("Path\tValue");
		for(int i=0; i<100;i++) {
			System.out.println(i + "\t" + stockAtTimeOne.get(i));
		}
	}

	/**
	 * Evaluates different options (European, Asian, Bermudan) using the given model.
	 *
	 * The options share the same maturity and strike for the at t=3.0.
	 * Observations which can be made:
	 * <ul>
	 * <li>The Asian is cheaper than the European since averaging reduces the volatility.
	 * <li>The European is cheaper than the Bermudan since exercises into the European is one (out of may) exercises strategies of the Bermudan.
	 * </ul>
	 *
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 */
	@Test
	public void testEuropeanAsianBermudanOption() throws CalculationException {
		/*
		 * Create the valuation model (see <code>getModel</code>)
		 */
		AssetModelMonteCarloSimulationModel model = getModel();

		/*
		 * Common parameters
		 */
		double maturity = 3.0;
		double strike = 1.07;

		/*
		 * European Option
		 */
		EuropeanOption myEuropeanOption = new EuropeanOption(maturity,strike);
		double valueOfEuropeanOption = myEuropeanOption.getValue(model);

		/*
		 * Asian Option
		 */
		double[] averagingPoints = { 1.0, 1.5, 2.0, 2.5 , 3.0 };

		AsianOption myAsianOption = new AsianOption(maturity,strike, new TimeDiscretizationFromArray(averagingPoints));
		double valueOfAsianOption = myAsianOption.getValue(model);

		/*
		 * Bermudan Option
		 */
		double[] exerciseDates	= { 1.0,  2.0,  3.0};
		double[] notionals		= { 1.20, 1.10, 1.0};
		double[] strikes		= { 1.03, 1.05, 1.07 };

		// Lower bound method
		BermudanOption myBermudanOptionLowerBound = new BermudanOption(exerciseDates, notionals, strikes, BermudanOption.ExerciseMethod.ESTIMATE_COND_EXPECTATION);
		double valueOfBermudanOptionLowerBound = myBermudanOptionLowerBound.getValue(model);

		// Upper bound method
		BermudanOption myBermudanOptionUpperBound = new BermudanOption(exerciseDates, notionals, strikes, BermudanOption.ExerciseMethod.UPPER_BOUND_METHOD);
		double valueOfBermudanOptionUpperBound = myBermudanOptionUpperBound.getValue(model);

		/*
		 * Output
		 */
		System.out.println("Value of Asian Option is \t"	+ valueOfAsianOption);
		System.out.println("Value of European Option is \t"	+ valueOfEuropeanOption);
		System.out.println("Value of Bermudan Option is \t"	+ "(" + valueOfBermudanOptionLowerBound + "," + valueOfBermudanOptionUpperBound + ")");

		Assert.assertTrue(valueOfAsianOption < valueOfEuropeanOption);
		Assert.assertTrue(valueOfBermudanOptionLowerBound < valueOfBermudanOptionUpperBound+1E-2);
		Assert.assertTrue(valueOfEuropeanOption < valueOfBermudanOptionUpperBound);
	}

	/**
	 * Evaluates 100000 Asian options in 10 parallel threads (each valuing 10000 options)
	 *
	 * @throws InterruptedException Thrown if multi-threadded execution is interrupted.
	 */
	public void testMultiThreaddedValuation() throws InterruptedException {
		final double[] averagingPoints = { 0.5, 1.0, 1.5, 2.0, 2.5, 2.5, 3.0, 3.0 , 3.0, 3.5, 4.5, 5.0 };
		final double maturity = 5.0;
		final double strike = 1.07;

		int			numberOfThreads	= 10;
		Thread[]	myThreads		= new Thread[numberOfThreads];

		for(int k=0; k<myThreads.length; k++) {

			final int threadNummer = k;

			// Create a runnable - piece of code which can be run in parallel.
			Runnable myRunnable = new Runnable() {
				@Override
				public void run() {
					try {
						for(int i=0;i<10000; i++) {
							AsianOption myAsianOption = new AsianOption(maturity,strike, new TimeDiscretizationFromArray(averagingPoints));
							double valueOfAsianOption = myAsianOption.getValue(model);
							System.out.println("Thread " + threadNummer + ": Value of Asian Option " + i + " is " + valueOfAsianOption);
						}
					} catch (CalculationException e) {
					}
				}
			};

			// Create a thread (will run asynchronously)
			myThreads[k] = new Thread(myRunnable);
			myThreads[k].start();
		}

		// Wait for all threads to complete
		for(int i=0; i<myThreads.length; i++) {
			myThreads[i].join();
		}

		// Threads are completed at this point
	}

	/**
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 */
	@Test
	public void testEuropeanCallDelta() throws CalculationException
	{
		/*
		 * Create the valuation model (see <code>getModel</code>)
		 */
		AssetModelMonteCarloSimulationModel model = getModel();

		// Java DecimalFormat for our output format
		DecimalFormat numberFormatStrike	= new DecimalFormat("     0.00 ");
		DecimalFormat numberFormatValue		= new DecimalFormat(" 0.00E00");
		DecimalFormat numberFormatDeviation	= new DecimalFormat("  0.00E00; -0.00E00");

		/*
		 * Cast the model to to get the parameters for analytic valuation
		 */
		double initialValue	= model.getAssetValue(0.0, 0).get(0);
		// @TODO This needs to be changes to use random variables.
		double riskFreeRate	= ((BlackScholesModel)((MonteCarloAssetModel)model).getModel()).getRiskFreeRate().getAverage();
		double volatility	= ((BlackScholesModel)((MonteCarloAssetModel)model).getModel()).getVolatility().getAverage();

		// Test options with different strike
		System.out.println("Calculation of Option Delta (European options with maturity 1.0):");
		System.out.println(" Strike \t MC Fin.Diff.\t MC Pathwise\t MC Likelihood\t Analytic \t Diff MC-FD \t Diff MC-PW \t Diff MC-LR");

		double optionMaturity	= 1.0;
		for(double optionStrike = 0.60; optionStrike < 1.50; optionStrike += 0.05) {

			// Create a product
			EuropeanOption		callOption	= new EuropeanOption(optionMaturity, optionStrike);

			// Value the product with Monte Carlo
			double shift = initialValue * 1E-6;

			Map<String,Object> dataUpShift = new HashMap<>();
			dataUpShift.put("initialValue", initialValue + shift);

			double valueUpShift	= (Double)(callOption.getValuesForModifiedData(model, dataUpShift).get("value"));

			Map<String,Object> dataDownShift = new HashMap<>();
			dataDownShift.put("initialValue", initialValue - shift);
			double valueDownShift	= (Double)(callOption.getValuesForModifiedData(model, dataDownShift).get("value"));

			// Calculate the finite difference of the monte-carlo value
			double delta = (valueUpShift-valueDownShift) / ( 2 * shift );

			// Calculate the finite difference of the analytic value
			double deltaFiniteDiffAnalytic	=
					(
							net.finmath.functions.AnalyticFormulas.blackScholesOptionValue(initialValue+shift, riskFreeRate, volatility, optionMaturity, optionStrike)
							- net.finmath.functions.AnalyticFormulas.blackScholesOptionValue(initialValue-shift, riskFreeRate, volatility, optionMaturity, optionStrike)
							)/(2*shift);

			// Calculate the analytic value
			double deltaAnalytic	= net.finmath.functions.AnalyticFormulas.blackScholesOptionDelta(initialValue, riskFreeRate, volatility, optionMaturity, optionStrike);


			// Print result
			System.out.println(numberFormatStrike.format(optionStrike) +
					"\t" + numberFormatValue.format(delta) +
					"\t" + numberFormatValue.format(deltaAnalytic) +
					"\t" + numberFormatDeviation.format(delta-deltaAnalytic));

			Assert.assertTrue(Math.abs(delta-deltaAnalytic) < 1E-02);
		}
		System.out.println("__________________________________________________________________________________________\n");
	}

	/**
	 * @throws CalculationException Thrown if s.th. went wrong during calculation (check getCause for details).
	 */
	@Test
	public void testEuropeanCallVega() throws CalculationException
	{
		/*
		 * Create the valuation model (see <code>getModel</code>)
		 */
		AssetModelMonteCarloSimulationModel model = getModel();

		// Java DecimalFormat for our output format
		DecimalFormat numberFormatStrike	= new DecimalFormat("     0.00 ");
		DecimalFormat numberFormatValue		= new DecimalFormat(" 0.00E00");
		DecimalFormat numberFormatDeviation	= new DecimalFormat("  0.00E00; -0.00E00");

		/*
		 * Cast the model to to get the parameters for analytic valuation
		 */
		double initialValue	= model.getAssetValue(0.0, 0).doubleValue();
		// @TODO This needs to be changes to use random variables.
		double riskFreeRate	= ((BlackScholesModel)((MonteCarloAssetModel)model).getModel()).getRiskFreeRate().doubleValue();
		double volatility	= ((BlackScholesModel)((MonteCarloAssetModel)model).getModel()).getVolatility().doubleValue();

		// Test options with different strike
		System.out.println("Calculation of Option Vega (European options with maturity 1.0):");
		System.out.println(" Strike \t MC Fin.Diff.\t Analytic \t Diff MC-FD");

		double optionMaturity	= 5.0;
		for(double optionStrike = 0.60; optionStrike < 1.50; optionStrike += 0.05) {

			// Create a product
			EuropeanOption		callOption	= new EuropeanOption(optionMaturity, optionStrike);

			// Value the product with Monte Carlo
			double shift = volatility * 1E-4;

			Map<String,Object> dataUpShift = new HashMap<>();
			dataUpShift.put("volatility", volatility + shift);

			double valueUpShift	= (Double)(callOption.getValuesForModifiedData(model, dataUpShift).get("value"));

			Map<String,Object> dataDownShift = new HashMap<>();
			dataDownShift.put("volatility", volatility - shift);
			double valueDownShift	= (Double)(callOption.getValuesForModifiedData(model, dataDownShift).get("value"));

			// Calculate the finite difference of the monte-carlo value
			double vega = (valueUpShift-valueDownShift) / ( 2 * shift );

			// Calculate the finite difference of the analytic value
			double vegaFiniteDiffAnalytic	=
					(
							net.finmath.functions.AnalyticFormulas.blackScholesOptionValue(initialValue+shift, riskFreeRate, volatility, optionMaturity, optionStrike)
							- net.finmath.functions.AnalyticFormulas.blackScholesOptionValue(initialValue-shift, riskFreeRate, volatility, optionMaturity, optionStrike)
							)/(2*shift);

			// Calculate the analytic value
			double vegaAnalytic	= net.finmath.functions.AnalyticFormulas.blackScholesOptionVega(initialValue, riskFreeRate, volatility, optionMaturity, optionStrike);

			// Print result
			System.out.println(numberFormatStrike.format(optionStrike) +
					"\t" + numberFormatValue.format(vega) +
					"\t" + numberFormatValue.format(vegaAnalytic) +
					"\t" + numberFormatDeviation.format(vega-vegaAnalytic));

			Assert.assertTrue(Math.abs(vega-vegaAnalytic) < 5E-02);
		}
		System.out.println("__________________________________________________________________________________________\n");
	}
}
