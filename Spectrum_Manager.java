import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

class MeanGenerator implements ActionListener
{
	public void actionPerformed( ActionEvent e )
	{
		IJ.showMessage("Clicked!");	
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
