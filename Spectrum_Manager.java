import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

import ij.measure.*;

//Generates the mean of all the currently selected ROIs
//Outputs them as a table, for all slices
class MeanGenerator implements ActionListener
{
	private RoiManager roi;
	private ImagePlus imp;
	private ResultsTable resultsWindow;

	public boolean ROIManagerCheck()
	{
		roi = RoiManager.getInstance();
		if( roi == null )
		{
			IJ.showMessage( "Error", "You must have the ROI manager open and some region selected" );
			return false;
		}
		return true;
	}

	public void actionPerformed( ActionEvent e )
	{
		//Requires ROI manager
		if( !ROIManagerCheck() ) return;
		imp = IJ.getImage();
		resultsWindow = new ResultsTable();
		resultsWindow.reset(); 


		ImageStatistics ims;
		for( int i = 1; i <= imp.getStackSize(); i++ )
		{
			resultsWindow.incrementCounter();

			for( int roi_num = 0; roi_num < roi.getCount(); roi_num++ )
			{
				roi.select( roi_num );
				imp.setSliceWithoutUpdate( i );
				ims = imp.getStatistics( Measurements.MEAN );

				resultsWindow.addValue( "Mean (" + Integer.toString(roi_num) + ")", ims.mean );
			}
		}

		resultsWindow.show( "Herbert" );
	}
}

public class Spectrum_Manager extends PlugInFrame {

	public Spectrum_Manager () 
	{
		super("Fretty Spectrum Manager");

		setLayout( new FlowLayout() );

		/////////////////////////////////////////////////////////
		//Setup gui and callbacks
		////////////////////////////////////////////////////////

		Button ROIOpener = new Button( "Open ROI Manager" );
		ROIOpener.addActionListener( 
			new ActionListener(){ 
				public void actionPerformed( ActionEvent e ) {
					if( RoiManager.getInstance() == null ) 
					{
						IJ.run( "ROI Manager..." );
					}
					else
					{
						RoiManager.getInstance().setVisible( true );
					}
				}
			} );
		add( ROIOpener );

		Button meanGenerator = new Button( "Calculate Spectrum" );
		meanGenerator.addActionListener( new MeanGenerator() );
		add( meanGenerator );

		pack();
		GUI.center(this);
		setVisible( true );
	}
}
