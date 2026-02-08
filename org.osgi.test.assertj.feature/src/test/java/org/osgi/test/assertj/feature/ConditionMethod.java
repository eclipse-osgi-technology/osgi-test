package org.osgi.test.assertj.feature;

public enum ConditionMethod {

	Has("to have"),
	DoesNotHas("not to have"),
	Is("to be"),
	IsNot("not to be");

	private String text;

	ConditionMethod(String text) {
		this.text = text;
	}

	@Override
	public String toString() {

		return text;
	}
}
