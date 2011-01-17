import ij.*;
import ij.measure.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

	public Spectrum3D( Spectrum3D old )
	{
		values = new float[old.values.length][old.values[0].length];
		
		for( int x = 0; x < values.length; x++ )
			for( int y = 0; y < values[0].length; y++ )
				values[x][y] = old.values[x][y];
	}

	public int getEmissionWavelengths()
	{
		return values[0].length;
	}
	
	public int getExcitationWavelengths()
	{
		return values.length;
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

	public void loadFromFile( String fileName ) throws	FileNotFoundException,
								IOException
	{
		clear();

		BufferedReader in = new BufferedReader( new FileReader(fileName) );
		
		List<String> lines = new ArrayList<String>();
		List<Float> vals = new ArrayList<Float>();

		String line;
		while( (line = in.readLine()) != null )
		{
			if( !line.isEmpty() )
				lines.add( line );
		}

		String[] string_values;
		for( String cur_line : lines )
		{
			string_values = cur_line.split("\t");

			for( String val : string_values )
				vals.add( Float.parseFloat(val) );
		}
		
		values = new float[lines.size()][ vals.size() / lines.size() ];
		
		for( int x = 0; x < values.length; x++ )
			for( int y = 0; y < values[0].length; y++ )
				values[x][y] = vals.get( ( x * values[x].length ) + y );

		in.close();
	}

	
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

	//Returns the sum of all values in the spectrum
	public float sum()
	{
		float total = 0;

		for( int x = 0; x < values.length; x++ )
			for( int y = 0; y < values[0].length; y++ )
				total += values[x][y];

		return total;				
	}

	public void normaliseTo( float quantumYield )
	{
		final float total = sum();

		for( int x = 0; x < values.length; x++ )
			for( int y = 0; y < values[0].length; y++ )
				values[x][y] = ( values[x][y] / total ) * quantumYield;
	}

	public float[][] asArray()
	{
		return values;
	}

}
