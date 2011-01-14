import ij.*;
import ij.measure.*;
import java.io.*;

//Not synchronised (dont think it matters for now)
//A 3d verison of the spectrum class
//instead of just having emission wavlengths and intensity values (a 1d array of floats) the
//3d version has both emission and excitation wavlengths and intesity values. (A 2d array of floats)
public class Spectrum3D
{
	float[][] values;
	
	public Spectrum3D( int excitationWavelengths, int emissionWavelengths )
	{
		values = new float[excitationWavelengths][emissionWavelengths];
		clear();
	}

	//public Spectrum( Spectrum old )
	//{
	//	values = new ArrayList<Float>(old.values);
	//}

	public int getEmissionWavelengths()
	{
		return values.length;
	}
	
	public int getExcitationWavelengths()
	{
		return values[0].length;
	}

	//Sets all the values of the spectrum to 0
	public void clear()
	{
		for( int x = 0; x < values.length; x++ )
			for( int y = 0; y < values[0].length; y++ )
				values[x][y] = 0;
	}

	public void setValue( int x, int y, float value )
	{
		values[x][y] = value;
	}

	public float getValue( int x, int y )
	{
		return values[x][y];
	}

	/*
	public void loadFromFile( String fileName ) throws	FileNotFoundException,
								IOException
	{
		clear();

		BufferedReader in = new BufferedReader( new FileReader(fileName) );
		
		String line;
		while( (line = in.readLine()) != null )
		{
			addValue( Float.parseFloat(line) );
		}
			
		in.close();
	}*/

	
	public void saveToFile( String fileName ) throws 	FileNotFoundException,
								IOException
	{
		PrintStream ps = new PrintStream( fileName );
				
		for( int x = 0; x < values.length; x++ )
		{
			ps.println();

			for( int y = 0; y < values[0].length; y++ )
			{	
				ps.print( values[x][y] );
				ps.print( '\t' );
			}
		}

		ps.close();		
	}

	public void displayInResultsWindow()
	{
		if( values.length <= 0 || values[0].length <= 0 ) return;

		ResultsTable window = new ResultsTable();
		window.reset();

		for( int y = 0; y < values[0].length; y++ )
		{
			window.incrementCounter();
			for( int x = 0; x < values.length; x++ )
			{
				window.addValue( Float.toString(x), values[x][y] );
			}
		}
		
		window.show( "Spectrum" );
	}

	/*
	public void normaliseTo( float quantumYield )
	{
		float total = 0;
		for( float val : values )
		{
			total += val;
		}
		
		for( int i = 0; i < values.size(); i++ )
		{
			values.set(i, (values.get(i) / total) * quantumYield );
		}
	}*/

	public float[][] asArray()
	{
		return values;
	}

}
