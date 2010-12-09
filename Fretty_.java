import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

import javax.swing.*;
import javax.swing.border.*;

import javax.*;
import javax.swing.plaf.*;

//The panel at the top of the gui screen...
class FrettyTopPanel extends JPanel
{
	//Padding
	private static final Insets insets = new Insets( 10,10,10,10 );
	public Insets getInsets() { return insets; }

	public FrettyTopPanel()
	{
		setLayout( new GridLayout( 4, 2 ) );
		add( new JLabel( "Cross Excitation Correction" ) );
		JCheckBox crossCorrection = new JCheckBox();
		add( crossCorrection );

		add( new JLabel("Wavelengths Per Sample  " ) );
		JSpinner wavelengthsPerSample = new JSpinner(
					new SpinnerNumberModel( 25, 1, 9999, 1 ) );
		add( wavelengthsPerSample );

		add( new JLabel( "Donor quantum yield" ) );
		JSpinner donorQuantumYield = new JSpinner( 
					new SpinnerNumberModel( (double)150, 0, 40000, 0.001 ) );
		add( donorQuantumYield );

		add( new JLabel( "Acceptor quantum yield" ) );
		JSpinner acceptorQuantumYield = new JSpinner( 
					new SpinnerNumberModel( (double)150, 0, 40000, 0.001 ) );
		add( acceptorQuantumYield );
	}
}

class FrettyCommonPanel extends JPanel 
{
	public FrettyCommonPanel()
	{
		setBorder( new TitledBorder("Common Tools") );
		add( new JButton("Open ROI Manager") );
		add( new JButton("Background Reduction" ) );
	}
}

class FrettyReferenceSpectraPanel extends JPanel
{
	public FrettyReferenceSpectraPanel()
	{
		setBorder( new TitledBorder("Reference Spectra") );
		
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) ); 
		add( new FrettySpectraSelector() );
		add( new FrettySpectraSelector() );
		add( new FrettySpectraSelector() );
	}
}

class FrettySpectraSelector extends JPanel
{
	JLabel label = new JLabel("SDD");
	JButton load = new JButton( "Load" );
	JButton create = new JButton( "Create" );
	JButton save = new JButton( "Save" );
	JButton view = new JButton( "View" );

	public FrettySpectraSelector()
	{	
		add( label );
		add( load );
		add( create );
		add( save );
		add( view );

		setEnabled( false );
	}

	public void setEnabled( boolean b ) 
	{
		label.setEnabled( b );
		load.setEnabled( b );
		create.setEnabled( b );
		save.setEnabled( b );
		view.setEnabled( b );
	}
}

class FrettyFRETSamplesPanel extends JPanel
{
	private static final Insets insets = new Insets( 5,5,5,5 );
	public Insets getInsets() { return insets; }
	public FrettyFRETSamplesPanel()
	{
		setBorder( new TitledBorder("FRET Samples") );
;
		setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;

		JPanel p1 = new JPanel();
		p1.setOpaque( false );
		p1.add( new JButton("Build Mega Stack") );
			JPanel wildcardPanel = new JPanel();
			wildcardPanel.setLayout( new GridLayout(2,1) );
			wildcardPanel.setBorder( new TitledBorder("Stack names (wildcard)") );
		
			wildcardPanel.add( new JTextField(15) );
			wildcardPanel.add( new JCheckBox("Include megastacks") );
		p1.add( wildcardPanel );
		add(p1, c);

		add( new JButton("Indicate donor excitation stack"), c ); 
		add( new JButton("Indicate acceptor excitation stack" ), c );
	}
}

public class Fretty_ extends PlugInFrame 
{
	public Fretty_ () 
	{
		super("Fretty");

		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS)) ;

		/////////////////////////////////////////////////////////
		//Setup gui and callbacks
		////////////////////////////////////////////////////////

		//We need swing for the panel borders, so make it look like the OS items
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e ){};

		add( new FrettyTopPanel() );
		add( new FrettyCommonPanel() );
		add( new FrettyReferenceSpectraPanel() );
		add( new FrettyFRETSamplesPanel() );

		JPanel p = new JPanel();
			p.add( new JButton( "Create FRET image" ) );
			p.add( new JButton( "ROI FRET Value" ) );

		add( p );

		pack();
		GUI.center(this);
		setVisible( true );
	}
}
