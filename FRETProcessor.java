import Jama.*;
import ij.*;

///////////////////////////////////////////////////////////////////////////
/// The class that does the actual work, to keep 
/// the processing out of the gui classes. 
//////////////////////////////////////////////////////////////////////////
public class FRETProcessor 
{
	boolean crossExcitationCorrection = true;
	int wavelengthsPerSample = 25;
	float donorQuantumYield = 0;
	float acceptorQuantumYield = 0;

	//Reference Spectra
	Spectrum SDD;
	Spectrum SAD;
	Spectrum SAA;

	ImagePlus donorExcitationStack;
	ImagePlus acceptorExcitationStack;

	public FRETProcessor()
	{
		
	}

	/////////////////////////////////////////////////
	//Boring setters and getters
	/////////////////////////////////////////////////

	void enableCrossExcitationCorrection( boolean yesno ) 
	{
		crossExcitationCorrection = yesno;
	}

	//Must be > 0 (durrrh). Returns false if value not accepted
	boolean setWavelengthsPerSample( int wlps )
	{
		if( wlps <= 0 ) return false;
		wavelengthsPerSample = wlps;
		return true;
	}

	int getWavelengthsPerSample()
	{
		return wavelengthsPerSample;
	}

	boolean setDonorQuantumYield( float val ) 
	{
		if( val < 0 || val >1 ) return false;
		donorQuantumYield = val;
		return true;
	}

	float getDonorQuantumYield()
	{
		return donorQuantumYield;
	}

	boolean setAcceptorQuantumYield( float val )
	{
		if( val < 0 || val > 1 ) return false;
		acceptorQuantumYield = val;
		return true;
	}

	float getAcceptorQuantumYield() 
	{
		return acceptorQuantumYield;
	}

	void setSDDSpectrum( Spectrum s )
	{
		SDD = s;
	}
	
	Spectrum getSDDSpectrum()
	{
		return SDD;
	}

	void setSADSpectrum( Spectrum s ) 
	{
		SAD = s;
	}

	Spectrum getSADSpectrum()
	{
		return SAD;
	}

	void setSAASpectrum( Spectrum s ) 
	{
		SAA = s;
	}

	Spectrum getSAASpectrum()
	{
		return SAA;
	}

	void setDonorExcitationStack( ImagePlus stack )
	{
		donorExcitationStack = stack;
	}

	ImagePlus getDonorExcitationStack()
	{
		return donorExcitationStack;
	}

	void setAcceptorExcitationStack( ImagePlus stack )
	{
		acceptorExcitationStack = stack;
	}

	ImagePlus getAcceptorExcitationStack()
	{
		return acceptorExcitationStack;
	}






	public void createFRETImage()
	{
		imageErrorChecks();

		//Normalise the required spectra to their quantum yield
		Spectrum SDDn = new Spectrum( SDD );
		Spectrum SADn = new Spectrum( SAD );

		SDDn.normaliseTo( donorQuantumYield );
		SADn.normaliseTo( acceptorQuantumYield );
			
		//combine the reference spectra into an array
		float[][] refs = new float[2][];
		refs [0] = SDDn.asArray();
		refs [1] = SADn.asArray();

		//double[] coefficients = findCoefficients( refs, 
	}

	// Adapted from Ben Corry's original code
	// R - Reference spectra
	// S - Combined/FRET spectra
	// All spectra must be of the same length
	// Returns an array of the coefficients 
	public double[] findCoefficients( float[][] R, float[] S )
	{
		//We need to reorganise all spectra to fit into some linear equations so
		//we can use the matrix library to solve for the coefficeints...
		//Eg. Ax = b, solving for x 

		final int numSpectra= R.length;		//Number of spectra we are trying to fit for
		final int numWavelengths = R[0].length;	//The number of wavelength samples per spectrum

		double[][] A = new double[numSpectra][numSpectra];
		double[] b = new double[numSpectra];

		for( int i = 0; i < numSpectra; i++ )
		{
			for( int j = 0; j < numSpectra; j++ )
			{
				A[i][j] = 0;
			}
			b[i] = 0;
		}

		//Fill the arrays with our linear equations 
		for( int i = 0; i < numWavelengths ; i++ )
		{
			for( int k = 0; k < numSpectra; k++ )
			{
				for( int j = 0; j < numSpectra; j++ )
				{
					A[k][j] = A[k][j] + R[j][i] * R[k][i];
				}
				b[k] = b[k] + S[i] * R[k][i];
			}
		}

		//Use jama to solve for the final coefficients matrix
		Matrix Alpha = new Matrix( A, 3, 3 );
		Matrix beta = new Matrix( b, 3 );

		Matrix x = Alpha.solve( beta );

		return x.getColumnPackedCopy();
	}

	//Checks that all params are setup correctly to call CreateFRETImage, throws 
	//approrpiate exceptions if something is wrong, with the message set.
	private void imageErrorChecks()
	{
		if( crossExcitationCorrection )
		{
			if( SAA == null )
				throw new NullPointerException( "No SAA spectrum set" );

			//Check that the SAA spectrum has the correct number of samples 
			if( SAA.getSize() != wavelengthsPerSample ) 
				throw new IllegalArgumentException( "The SAA spectrum doesn't have the correct number of samples (has " +
									SAA.getSize() + " needs " + wavelengthsPerSample + ")" );

			//Check the acceptor excitation stack is okay/legit
			if( acceptorExcitationStack == null ) 
				throw new NullPointerException( "No acceptor excitation stack indicated" );

			//acceptor stack must be the same size as the donor stack
			if( donorExcitationStack != null && acceptorExcitationStack.getStackSize() != donorExcitationStack.getStackSize() )
				throw new IllegalArgumentException( "The acceptor stack and donor stack must be of the same size! donor " + 
									"stack has " + donorExcitationStack.getStackSize() + " slices, and acceptor " + 
									" stack has " + acceptorExcitationStack.getStackSize() );
		}

		//Check that the SDD and SAD spectrums exist
		if( SDD == null ) 
			throw new NullPointerException( "No SDD spectrum set" );
		if( SAD == null ) 
			throw new NullPointerException( "No SAD spectrum set" );

		//Check that the SDD and SAD spectrums have the correct number of samples
		if( SDD.getSize() != wavelengthsPerSample )
			throw new IllegalArgumentException( "The SDD spectrum doesn't have the correct number of samples (has " + 
								SDD.getSize() + " needs " + wavelengthsPerSample + ")" );
		
		if( SAD.getSize() != wavelengthsPerSample )
			throw new IllegalArgumentException( "The SAD spectrum doesn't have the correct number of samples (has " + 
								SAD.getSize() + " needs " + wavelengthsPerSample + ")" );	

		//Check the stacks are set correctly. 
		if( donorExcitationStack == null ) 
			throw new NullPointerException( "The donor excitation stack has not been set." );
		
		//Check that the donor stack is a multiple of the wavelengthspersample ( so we can accept mega stacks );
		if( donorExcitationStack.getStackSize() % wavelengthsPerSample != 0 )
			throw new IllegalArgumentException( "The donor excitation stack does not have the correct number of " + 
								"slices, it must be a multiple of the wavelengths per sample." );


	}
}
