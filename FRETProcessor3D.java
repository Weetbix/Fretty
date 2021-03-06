import Jama.*;
import ij.*;
import ij.process.*;

///////////////////////////////////////////////////////////////////////////
/// The class that does the actual work, to keep 
/// the processing out of the gui classes. 
/// This class is the 3D FRET processor
//////////////////////////////////////////////////////////////////////////
public class FRETProcessor3D 
{
	int excitationWavelengths = 4;
	float donorQuantumYield = 0.5f;
	float acceptorQuantumYield = 0.5f;

	//Reference Spectra
	Spectrum3D SD;
	Spectrum3D SA;

	ImagePlus donorExcitationStack;

	public FRETProcessor3D()
	{
		
	}

	/////////////////////////////////////////////////
	//Boring setters and getters
	/////////////////////////////////////////////////

	boolean setExcitationWavelengths( int wls )
	{
		if( wls <= 0 ) return false;
		excitationWavelengths = wls;
		return true;
	}

	int getExcitationWavelengths()
	{
		return excitationWavelengths;
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

	void setSDSpectrum( Spectrum3D s )
	{
		SD = s;
	}
	
	Spectrum3D getSDSpectrum()
	{
		return SD;
	}

	void setSASpectrum( Spectrum3D s ) 
	{
		SA = s;
	}

	Spectrum3D getSASpectrum()
	{
		return SA;
	}

	void setDonorExcitationStack( ImagePlus stack )
	{
		donorExcitationStack = stack;
	}

	ImagePlus getDonorExcitationStack()
	{
		return donorExcitationStack;
	}

	///////////////////////////////////////////////////////////////////////////
	// Begin methods that actually do important stuff
	////////////////////////////////////////////////////////////////////////////

	//Creates the 3D Fret spectrum from donor only and acceptor only spectra
	//SDn and SAn should be normalised acceptor and donor spectra
	public Spectrum3D createFRETSpectrum( Spectrum3D SDn, Spectrum3D SAn )
	{
		//save the raw arrays for quick access 
		float SDnArray[][] = SDn.asArray();
		float SAnArray[][] = SAn.asArray();

		final int emissionWavelengths =  SDn.getEmissionWavelengths();
		final int excitationWavelengths = SDn.getExcitationWavelengths();
	
		float sum_d[] = new float[excitationWavelengths];
		float sum_a[] = new float[emissionWavelengths];
	
		////////////////////////////////////////////////////////////////
		//Create the FRET spectrum 
		/////////////////////////////////////////////////////////////////

		for( int x = 0; x < excitationWavelengths; x++ )
			for( int y = 0; y < emissionWavelengths; y++ )
				sum_d[x] = sum_d[x] + SDnArray[x][y];


		for( int x = 0; x < excitationWavelengths; x++ )
			for( int y = 0; y < emissionWavelengths; y++ )
				sum_a[y] = sum_a[y] + SAnArray[x][y];

		Spectrum3D FRETSpectrum = new Spectrum3D( excitationWavelengths, emissionWavelengths );
		float FRETSpectrumArray[][] = FRETSpectrum.asArray();
	
		for( int y = 0; y < emissionWavelengths; y++ )
			for( int x = 0; x < excitationWavelengths; x++ )
				FRETSpectrum.setValue( x, y, sum_d[x] * sum_a[y] );

		return FRETSpectrum;
	}

	//Returns an E value using the current settings of this object, and given a
	//single 3D FRET spectrum.
	// S is the combined FRET spectrum. Eg the spectrum from the fret image
	public double findEValue( Spectrum3D S )
	{

		findEValueChecks();

		//normalise the reference spectra to their respectful quantum yield
		Spectrum3D SDn = new Spectrum3D( SD );
		Spectrum3D SAn = new Spectrum3D( SA );

		SDn.normaliseTo( donorQuantumYield );
		SAn.normaliseTo( acceptorQuantumYield );

		Spectrum3D FRETSpectrum = createFRETSpectrum( SDn, SAn );

		//combine the spectras into a nice array that we can use with findCoefficients
		float[][][] refs = new float[3][][];
		refs[0] = SDn.asArray();
		refs[1] = SAn.asArray();
		refs[2] = FRETSpectrum.asArray();

		double[] coefficients = findCoefficients( refs, S.asArray() );

		// SDA = dSD + aSA + fSF + bB
		// and E = f / d + f
		return coefficients[2] / ( coefficients[0] + coefficients[2] );
	}

	//Creates an image of E values for each pixel in a FRET image.
	//Potential optimisations:
	//	- save references to pixel arrays instead of using getPixelValue/setPixelValue
	//	- will require require the image to be in 1 specific format, eg 32-bit
	public void createFRETImage()
	{
		imageErrorChecks();

		//Loop through each pixel in the image stack. For each pixel make a 3D Spectrum.
		// for each of these spectra pass them to findCoefficients
		//For each e value returned, we can create a new image.

		final int numSpectra = donorExcitationStack.getWidth() * donorExcitationStack.getHeight();

		//normalise the reference spectra to their respectful quantum yield
		Spectrum3D SDn = new Spectrum3D( SD );
		Spectrum3D SAn = new Spectrum3D( SA );

		SDn.normaliseTo( donorQuantumYield );
		SAn.normaliseTo( acceptorQuantumYield );

		Spectrum3D FRETSpectrum = createFRETSpectrum( SDn, SAn );

		//Combine the reference spectra into an array so we can use findCoefficients
		float[][][] refs = new float[3][][];
		refs[0] = SDn.asArray();
		refs[1] = SAn.asArray();
		refs[2] = FRETSpectrum.asArray();

		//Create the blank image for our 'E-Value' image.
		ImagePlus newImage = IJ.createImage( 	"E value image",
								"32-bit",
								donorExcitationStack.getWidth(),
								donorExcitationStack.getHeight(), 1 );
	
		newImage.hide();
		ImageProcessor image = newImage.getProcessor();

		//Cache the processors for each image in the stack 
		ImageProcessor[] processors = new ImageProcessor[ donorExcitationStack.getStackSize() ];
		for( int i = 0; i < donorExcitationStack.getStackSize(); i++ )
		{
			processors[i] = donorExcitationStack.getStack().getProcessor(i + 1);
		}
		
		
		final int emissionWavelengths =  SDn.getEmissionWavelengths();
		final int excitationWavelengths = SDn.getExcitationWavelengths();
		final int numSlices = donorExcitationStack.getStack().getSize();

		float[][] spectrum = new float[ excitationWavelengths ][ emissionWavelengths ];
		//for each 'pixel' or spectrum....
		for( int specNum = 0; specNum  < numSpectra; specNum  ++ )
		{
			//calculate which pixel we are currently working with..
			final int x = specNum % donorExcitationStack.getWidth();
			final int y = specNum / donorExcitationStack.getHeight();
			
			int emWavelength = 0;				//current emission wavelength
			int exWavelength = -1;				//current excitation wavelength

			//for each 'slice' in the stack
			for( int slice= 0; slice < numSlices; slice++ )
			{
				if( ( slice ) % emissionWavelengths == 0 )
					exWavelength++;

				emWavelength = (slice) % emissionWavelengths ;

				spectrum[ exWavelength ][ emWavelength ] = processors[slice].getPixelValue( x, y );
			}

			//now that we have a spectrum for this pixel, put it through the coefficients solver...
			double[] coefficients = findCoefficients( refs, spectrum );

			float e = (float) (coefficients[2] / ( coefficients[0] + coefficients[2] ));

			//set the pixel...
			image.putPixelValue( x, y, e );
		}

		newImage.show();
	}

	// Adapted from Ben Corry's original code
	// R - Reference spectra
	// S - Combined/FRET spectra
	// All spectra must be of the same length
	// Returns an array of the coefficients 
	public double[] findCoefficients( float[][][] R, float[][] S )
	{
   
		//We need to reorganise all spectra to fit into some linear equations so
		//we can use the matrix library to solve for the coefficeints...
		//Eg. Ax = b, solving for x 
		
		final int numSpectra= R.length;		//Number of spectra we are trying to fit for
		final int numEmissions = R[0][0].length;	//The number of wavelength samples per spectrum
		final int numExcitations = R[0].length;		//The number of excitation wavelengths per spectrum

		double[][] A = new double[numSpectra][numSpectra];
		double[] b = new double[numSpectra];

		for( int i = 0; i < numExcitations; i++ )
		{
			for( int j = 0; j < numEmissions; j++ )
			{
				for( int k = 0; k < numSpectra; k++ )
				{
					for( int m = 0; m < numSpectra; m++ )
					{
						A[k][m] = A[k][m] + R[k][i][j] * R[m][i][j];
					}
					b[k]= b[k] + S[i][j] * R[k][i][j];
				}
			}

		}

		//use jama to solve for the final coefficients matrix
		Matrix Alpha = new Matrix( A, numSpectra, numSpectra );
		Matrix beta = new Matrix( b, numSpectra );

		Matrix x = Alpha.solve( beta );

		return x.getColumnPackedCopy();
	}

	//Checks that all params are setup correctly to call findEValue. 
	//Checks are a little different to createImage
	private void findEValueChecks()
	{
		basicErrorChecks();

		//Check that the donor stack is EXACTLY the size of the wavelengthspersample
		//This is because you need to select ROIs and these wont match for mega stacks!
		if( SD.getExcitationWavelengths() * SD.getEmissionWavelengths() != donorExcitationStack.getStackSize() )
			throw new IllegalArgumentException( "The donor stack must be the correct size (excitationWavs * EmissionWavs)" );
	}

	//Checks that all params are setup correctly to call CreateFRETImage, throws 
	//approrpiate exceptions if something is wrong, with the message set.
	private void imageErrorChecks()
	{
		basicErrorChecks();
		
		//Check that the donor stack is a multiple of the excitationWavelengths ( so we can accept mega stacks );
		//The stack must be atleast <excitation wavelengths> big
		if( donorExcitationStack.getStackSize() % excitationWavelengths != 0 )
			throw new IllegalArgumentException( "The donor excitation stack does not have the correct number of " + 
								"slices, it must be a multiple of the excitationWavelengths." );

		if( donorExcitationStack.getStackSize() < excitationWavelengths )
			throw new IllegalArgumentException( "The donor excitation stack does not have enough slices, it must have atleast excitationWavelengths slices" );

		//Check that the donor stack has the correct number of samples, that also allow it to be a megastack. 
		if( (donorExcitationStack.getStackSize() / excitationWavelengths ) % SA.getEmissionWavelengths() != 0 )
			throw new IllegalArgumentException( "The donor excitation stack does not have the correct number of slices" );
	}

	//checks that all basic params are setup correctly to call either createIMage or getEvalue
	//basically this function contains shared error checks. 
	private void basicErrorChecks()
	{
		//Check that the SD and SA spectrums exist
		if( SD == null ) 
			throw new NullPointerException( "No SD spectrum set" );
		if( SA == null ) 
			throw new NullPointerException( "No SA spectrum set" );

		//Check that the SD and SA spectrums have the correct number of samples
		if( SD.getExcitationWavelengths() != excitationWavelengths )
			throw new IllegalArgumentException( "The SD Spectrum doesn't have the correct number of excitaiton wavelengths ( has " + 
									SD.getExcitationWavelengths() + " needs " + excitationWavelengths + ")" );

		if( SA.getExcitationWavelengths() != excitationWavelengths )
			throw new IllegalArgumentException( "The SD Spectrum doesn't have the correct number of excitaiton wavelengths ( has " + 
									SD.getExcitationWavelengths() + " needs " + excitationWavelengths + ")" );

		if( SD.getEmissionWavelengths() != SA.getEmissionWavelengths() ) 
			throw new IllegalArgumentException( "The SA and SD Spectra do not have the same number of emission wavlengths. (SA has " + 
									SA.getEmissionWavelengths() + " and SD has " + SD.getEmissionWavelengths() + ")" );

		//Check the stacks are set correctly. 
		if( donorExcitationStack == null ) 
			throw new NullPointerException( "The donor excitation stack has not been set." );
		
	}
}
