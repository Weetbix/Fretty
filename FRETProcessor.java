import ij.*;

///////////////////////////////////////////////////////////////////////////
/// The class that does the actual work, to keep 
/// the processing out of the gui classes. 
//////////////////////////////////////////////////////////////////////////
public class FRETProcessor 
{
	boolean crossExcitationCorrection = true;
	int wavelengthsPerSample = 5;
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

	public void CreateFRETImage()
	{
		
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






	//Checks that all params are setup correctly to call CreateFRETImage, throws 
	//approrpiate exceptions if something is wrong, with the message set.
	private void imageErrorChecks()
	{
		if( crossExcitationCorrection )
		{
			if( acceptorExcitationStack == null ) 
				throw new NullPointerException( "No acceptor excitation stack indicated" );

			if( SAA == null )
				throw new NullPointerException( "No SAA spectrum loaded" );
		}

		
	}
}
