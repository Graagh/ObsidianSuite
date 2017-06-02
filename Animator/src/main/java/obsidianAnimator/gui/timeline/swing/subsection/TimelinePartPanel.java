package obsidianAnimator.gui.timeline.swing.subsection;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import obsidianAPI.render.part.Part;

public class TimelinePartPanel extends JPanel
{
	
	protected JLabel partName;
	protected PartSpinner xSpinner, ySpinner, zSpinner;
	private TimelinePartController controller;
	
	public TimelinePartPanel(TimelinePartController controller)
	{		
		this.controller = controller;
		
		partName = new JLabel();
		xSpinner = new PartSpinner(0);
		ySpinner = new PartSpinner(1);
		zSpinner = new PartSpinner(2);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(1,1,1,1);
		
		c.gridwidth = 2;
		add(partName,c);
		
		c.gridwidth = 1;
		c.gridy = 1;
		c.gridx = 0;
		add(new JLabel("X:"),c);
		c.gridx = 1;
		add(xSpinner,c);

		c.gridy = 2;
		c.gridx = 0;
		add(new JLabel("Y:"),c);
		c.gridx = 1;
		add(ySpinner,c);
		
		c.gridy = 3;
		c.gridx = 0;
		add(new JLabel("Z:"),c);
		c.gridx = 1;
		add(zSpinner,c);

		setBorder(BorderFactory.createTitledBorder("Part"));
	}
	
	class PartSpinner extends JSpinner {
		
		private int dimension;
		
		private PartSpinner(int dimension) {
			super(new PartSpinnerModel());
			this.dimension = dimension;

			addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent e) {
					Part part = controller.getSelectedPart();
					PartSpinner spinner = (PartSpinner) e.getSource();
					if(part != null) {
						double d = (double) spinner.getValue();						
						part.setValue((float) d, dimension);
					}
				}
			});
		}
	}
	
	private class PartSpinnerModel extends SpinnerNumberModel {
		
		private PartSpinnerModel() {
			super(0, -3.14, 3.14, 0.01);
		}
		
		@Override
	    public Object getNextValue() {
	        return incrValue(+1);
	    }
		
		@Override
	    public Object getPreviousValue() {
	        return incrValue(-1);
	    }
		
		private double incrValue(int dir)
	    {
			double current = ((Number) getValue()).doubleValue();
			double stepSize = ((Number) getStepSize()).doubleValue();
			double maximum = ((Number) getMaximum()).doubleValue();
			double minimum = ((Number) getMinimum()).doubleValue();
			
			double newValue = current + stepSize * (double)dir;

	        if (newValue > maximum)
	            return minimum;
	        else if (newValue < minimum)
	            return maximum;
	        else
	            return newValue;
	    }
	}
	
}
