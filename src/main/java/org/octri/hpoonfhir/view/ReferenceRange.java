package org.octri.hpoonfhir.view;

/**
 * A reference range for a test that indicates that minimum and maximum values
 * that are considered normal
 * 
 * @author yateam
 *
 */
public class ReferenceRange {

	private Double min;
	private Double max;

	public ReferenceRange(Double min, Double max) {
		this.min = min;
		this.max = max;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

}
