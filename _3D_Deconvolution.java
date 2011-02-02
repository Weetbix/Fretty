import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.event.*;
import javax.*;

//The panel at the top of the gui screen...
class FrettyTopPanel3D extends JPanel
{
	_3D_Deconvolution mainGUI;
	FRETProcessor3D processor;

	//Padding
	private final Insets insets = new Insets( 10,10,10,10 );
	public Insets getInsets() { return insets; }

	public FrettyTopPanel3D( _3D_Deconvolution frettyGUI, FRETProcessor3D fretp )
	{
		mainGUI = frettyGUI;
		processor = fretp;				
		setLayout( new GridLayout( 3, 2 ) );

		//How many excitation wavelengths there are per image stack
		add( new JLabel("Excitation Wavelengths " ) );
		final JSpinner excitationWavelengths = new JSpinner( new SpinnerNumberModel( 4, 1, 9999, 1 ) );
		excitationWavelengths .addChangeListener( 
			new ChangeListener(){
				public void stateChanged( ChangeEvent e ) {
					processor.setExcitationWavelengths( (Integer) excitationWavelengths.getValue() );
				}
			});
		add( excitationWavelengths );

		add( new JLabel( "Donor quantum yield" ) );
		JSpinner donorQuantumYield = new JSpinner( 
			new SpinnerNumberModel( (double) processor.getDonorQuantumYield(), 0, 1, 0.001 ) );
		donorQuantumYield.addChangeListener(
			new ChangeListener(){
				public void stateChanged( ChangeEvent e ) {
					final JSpinner source = (JSpinner) e.getSource();
					processor.setDonorQuantumYield( ((Double)source.getModel().getValue()).floatValue() );
				}
			});
		add( donorQuantumYield );

		add( new JLabel( "Acceptor quantum yield" ) );
		JSpinner acceptorQuantumYield = new JSpinner( 
			new SpinnerNumberModel( (double) processor.getAcceptorQuantumYield(), 0,1, 0.001 ) );
		acceptorQuantumYield.addChangeListener(
			new ChangeListener(){
				public void stateChanged( ChangeEvent e ){
					final JSpinner source = (JSpinner) e.getSource();
					processor.setAcceptorQuantumYield( ((Double) source.getValue()).floatValue() );
				}
			});
		add( acceptorQuantumYield );
	}
}

class FrettyCommonPanel3D extends JPanel 
{
	public FrettyCommonPanel3D()
	{
		JTabbedPane tabPanel = new JTabbedPane();

		JPanel commonPanel = new JPanel();

		//Opens the ROI manager
		JButton roiMan = new JButton( "Open ROI Manager" );
		roiMan.addActionListener( 
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
		commonPanel.add( roiMan );

		//Does background reduction on the current stack
		JButton bgReduction = new JButton( "Background Reduction" );
		bgReduction.addActionListener(
			new ActionListener(){
				public void actionPerformed( ActionEvent e ){
					IJ.run( "Background Reduction" );
				}
			}
		);
		
		commonPanel.add( bgReduction );

		tabPanel.addTab( "Common Tools", commonPanel );
		tabPanel.addTab( "Batching", new BatchingPanel() );
		add( tabPanel );
	}
}

class FrettyReferenceSpectraPanel3D extends JPanel
{
	FrettySpectraSelector3D SD;
	FrettySpectraSelector3D SA;

	public FrettyReferenceSpectraPanel3D( final FRETProcessor3D processor )
	{
		setBorder( new TitledBorder("Reference Spectra") );
		
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) ); 
		SD =  new FrettySpectraSelector3D( "SD", processor );
		SA = 	new FrettySpectraSelector3D( "SA", processor );


		//set all the callbacks, so the processor knows when a spectrum has changed...
		SD.setListener( new FrettySpectraSelector3D.SpectrumChangedEvent(){
			public void onChange(){
				processor.setSDSpectrum( SD.getSpectrum() );
			}
		});

		SA.setListener( new FrettySpectraSelector3D.SpectrumChangedEvent(){
			public void onChange(){
				processor.setSASpectrum( SA.getSpectrum() );
			}
		});


		add( SD );
		add( SA );
	}
}

class FrettySpectraSelector3D extends JPanel
{
	public interface SpectrumChangedEvent 
	{
		public void onChange();
	}

	JLabel label;
	JButton load = new JButton( "Load" );
	JButton create = new JButton( "Create" );
	JButton save = new JButton( "Save" );
	JButton view = new JButton( "View" );
	JButton clear = new JButton( "Clear" );

	//The actual spectrum object associated with this GUI panel
	Spectrum3D spectrum;
	SpectrumChangedEvent listener;
	FRETProcessor3D processor;

	public FrettySpectraSelector3D( String spectrumName, FRETProcessor3D proc )
	{	
		processor = proc;
		label = new JLabel( spectrumName );
		label.setForeground( Color.red ); 

	
		//CREATE BUTTON
		create.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent actionEvent ){
				if( spectrum == null )
					setSpectrum( SpectrumGenerator.generate3DFromROI( processor.getExcitationWavelengths() ) );
				else 
				{
					//We dont want to accidentally erase the spectrum they have already
					//added if they click create and there is an error (eg ROI man isnt open)
					Spectrum3D s = SpectrumGenerator.generate3DFromROI( processor.getExcitationWavelengths() );
					if( s != null ) 
						setSpectrum( s );
				}
			}
		});

		//LOAD BUTTON
		load.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				OpenDialog od = new OpenDialog("Select a spectrum file", "");
	
				//Steal the focus back from the main imagej window :(
				requestFocus();

				//If they didnt open a file, bail. 
				if( od.getFileName() == null ) return;

				Spectrum3D loaded_spec = new Spectrum3D(4,4);
				//Try to load the file and report any error messages
				try
				{
					loaded_spec.loadFromFile( od.getDirectory() + od.getFileName() );
					setSpectrum( loaded_spec );
				}
				catch( Exception ex )
				{
					IJ.showMessage( "Couldnt load the spectrum file: " + ex.getMessage() );
					setSpectrum( null );
				}
			}
		});

		//SAVE BUTTON
		save.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				if( spectrum != null )
				{
					SaveDialog sd = new SaveDialog ("Select a spectrum file", "", ".spec3d" );
	
					//Steal the focus back from the main imagej window :(
					requestFocus();

					//If they didnt open a file, bail. 
					if( sd.getFileName() == null ) return;

					//Try to save the file and report any error messages
					try
					{
						spectrum.saveToFile( sd.getDirectory() + sd.getFileName() );
					}
					catch( Exception ex )
					{
						IJ.showMessage( "Couldnt load the spectrum file: " + ex.getMessage() );
					}
				}
			}
		});

		//VIEW BUTTON
		view.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				if( spectrum != null ) spectrum.displayInResultsWindow();
			}

		});

		//CLEAR BUTTON
		clear.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				setSpectrum( null );
			}
		});

		Insets buttonMargin = new Insets( 2,4,2,4 );
		create.setMargin( 	buttonMargin  );
		load.setMargin( 	buttonMargin );
		save.setMargin( 	buttonMargin );
		view.setMargin( 	buttonMargin );
		clear.setMargin( 	buttonMargin );

		add( label );
		add( create );
		add( load );
		add( save );
		add( view );
		add( clear );

		setSpectrum( null );
	}

	public void setListener( SpectrumChangedEvent newListener ) 
	{
		listener = newListener;
	}

	//Sets the spectrum associated with this GUI panel.
	//If the spectrum is set to null then the text will go red, if the spectrum is okay
	//to use the text will go green.
	public void setSpectrum( Spectrum3D spec )
	{
		spectrum = spec;
		
		//notify the callback if there is one...
		if( listener != null ) listener.onChange();

		if( spectrum == null )
		{
			label.setForeground( Color.red );

			//dont show these buttons without a spectrum
			save.setEnabled( false );
			view.setEnabled( false );
			clear.setEnabled( false );
		}
		else
		{
			//Empty spectrums do us no good...
			if( spectrum.getExcitationWavelengths() <= 0 || spectrum.getEmissionWavelengths() <= 0  ) setSpectrum( null );

			label.setForeground( Color.green );

			save.setEnabled( true );
			view.setEnabled( true );
			clear.setEnabled( true );
		}
	}

	public Spectrum3D getSpectrum()
	{
		return spectrum;
	}
}

class FrettyFRETSamplesPanel3D extends JPanel
{
	//The two stack selector buttons use this class
	class StackSelectorButton extends JButton
	{
		private ImagePlus stack;
		private Color currentColour = Color.red;

		StackSelectorButton( String title ) 
		{
			super( title ); 
			setStack( null );

			//Add a 'clear' button to clear the stack selection
			final JPopupMenu menu = new JPopupMenu();
			JMenuItem clear = new JMenuItem( "Clear" );
			clear.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e )
				{
					setStack( null );
				}
			});
			menu.add( clear );

			//Add a 'show stack' button to bring the stack to the front
			JMenuItem showStack = new JMenuItem( "Show stack" );
			showStack.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e )
				{
					if( stack != null )
						stack.getWindow().show();
				}
			});
			menu.add( showStack );

			addMouseListener( new MouseAdapter(){
				public void mouseReleased( MouseEvent e )
				{
					if( e.isPopupTrigger() )
						menu.show( e.getComponent(), e.getX(), e.getY() );
				}
			});
		}

		@Override
		public void setEnabled( boolean enable )
		{
			//Need to override this otherwise the disabled button has a green/red label even
			//if it is disabled :(

			if( enable == false )
				setForeground( Color.gray );
			else 
				setForeground( currentColour );

			super.setEnabled( enable );
		}

		public void setStack( ImagePlus newstack) 
		{
			//Sets the stack and adjusts the tooltip for the button
			stack = newstack;
				
			if( stack != null )
			{
				setToolTipText( "Set to: " + stack.getTitle() );
				currentColour = Color.green;
				setForeground( currentColour  );
			}
			else
			{
				setToolTipText( "Not set" );
				currentColour = Color.red;
				setForeground( currentColour  );
			}
		}

		public ImagePlus getStack()
		{
			return stack;
		}
	}

	//GUI members
	private StackSelectorButton donorStackButton;
	private StackSelectorButton acceptorStackButton;
	private final Insets insets = new Insets( 5,5,5,5 );
	public Insets getInsets() { return insets; }

	FRETProcessor3D processor;

	public FrettyFRETSamplesPanel3D( final FRETProcessor3D the_processor )
	{
		setBorder( new TitledBorder("FRET Sample") );
		processor = the_processor; 

		donorStackButton = new StackSelectorButton("Indicate donor excitation stack");
		donorStackButton.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				ImagePlus newstack = WindowManager.getCurrentImage();
				donorStackButton.setStack( newstack );
				processor.setDonorExcitationStack( newstack );
			}
		});

		JPanel buttonPanel = new JPanel();
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets( 8, 5, 5, 5 );
		buttonPanel.setLayout( new GridBagLayout() );
		buttonPanel.setOpaque( false );

		buttonPanel.add( donorStackButton, c );

		add( buttonPanel );
	}

	//update the links between the processors stacks and the ones the buttons know about
	public void updateProcessor()
	{
		processor.setDonorExcitationStack( donorStackButton.getStack() );
	}
}

public class _3D_Deconvolution extends PlugInFrame 
{
	FrettyTopPanel3D 				topPanel;
	FrettyCommonPanel3D 			commonPanel;
	FrettyReferenceSpectraPanel3D		referenceSpectraPanel;
	FrettyFRETSamplesPanel3D 			FRETSamplesPanel;

	//The main class that does all the FRET work when you hit go
	FRETProcessor3D processor = new FRETProcessor3D();

	public _3D_Deconvolution () 
	{
		super("3D Deconvolution");

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

		topPanel = new FrettyTopPanel3D(this, processor);
		commonPanel = new FrettyCommonPanel3D();
		referenceSpectraPanel = new FrettyReferenceSpectraPanel3D( processor );
		FRETSamplesPanel = new FrettyFRETSamplesPanel3D( processor );

		add( topPanel ); 
		add( commonPanel );
		add( referenceSpectraPanel );
		add( FRETSamplesPanel );

		JPanel p = new JPanel();
			//The GO button!
			JButton b = new JButton( "Create FRET image" );
			b.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e ) 
				{
					try
					{
						long start = System.currentTimeMillis();
	
						FRETSamplesPanel.updateProcessor();
						processor.createFRETImage();

						long elapsedMillis = System.currentTimeMillis() - start;
			
						float seconds = elapsedMillis / 1000F;

						IJ.showMessage( "Milliseconds", "=" + elapsedMillis );
					}
					catch( Exception ex ) 
					{
						JOptionPane.showMessageDialog( 	null, 
											ex.getMessage(), 
											"Error",
											JOptionPane.ERROR_MESSAGE );
					}
				}
			});
			p.add( b );

			b = new JButton( "ROI FRET Values" );
			
			b.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e )
				{
					try
					{
						FRETSamplesPanel.updateProcessor();
						ImagePlus donorStack = processor.getDonorExcitationStack();
				
						if( donorStack == null )
						{
							JOptionPane.showMessageDialog( 	null, 
									"You need to select a donor stack", 
									"Error",
									JOptionPane.ERROR_MESSAGE );
							return;
						}

						//get a list of FRET spectra from the current stack and ROIs
						Spectrum3D[] spectra = SpectrumGenerator.array3DFromROI( donorStack, processor.getExcitationWavelengths() );
					
						if( spectra == null || spectra.length <= 0 ) return;
						
						//Get the results ready so we can have something to fill the table with...
						double[] evals = new double[spectra.length];
						for( int i = 0; i < spectra.length; i++ )
						{
							evals[i] = processor.findEValue( spectra[i] );
						}

						double average = 0;
						for( double evalue : evals )
						{
							average += evalue;
						}
						average = average / spectra.length;

						//Get a results table ready to fill
						ResultsTable window = new ResultsTable();
						window.reset();
						window.setPrecision( 10 );

						for( int roi_num = 0; roi_num < spectra.length; roi_num++ )
						{
							window.incrementCounter();
							window.addValue( "ROI", roi_num + 1 );
							window.addValue( "E", evals[roi_num] );
							window.addValue( "Mean", average );
						}

						window.show( "E values for all FRET ROIs" );
					}
					catch( Exception ex )
					{
						JOptionPane.showMessageDialog( 	null, 
											ex.getMessage(), 
											"Error",
											JOptionPane.ERROR_MESSAGE );
					}
				}
			});
			p.add( b );

		add( p );

		pack();
		GUI.center(this);
		setVisible( true );
	}
}
