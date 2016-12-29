package com.se.tool;

import java.util.ArrayList;
import java.util.List;

public class TerminalToHtml {

	private int index = 0;
	private List<Line> body = new ArrayList<Line>();

	public TerminalToHtml append(String line) {
		index = body.size();
		String[] split = line.split("\n");
		int length = split.length;
		for (int i = 0; i < length; i++) {
			Line content = new Line();
			content.string = split[i];
			body.add(content);
		}
		return this;
	}

	public TerminalToHtml setColor(int r, int g, int b) {
		int size = body.size();
		for (int i = index; i < size; i++) {
			Line content = body.get(i);
			content.color = (r * 256 + g) * 256 + b;
		}

		return this;
	}

	public TerminalToHtml setBold(boolean bold) {
		for (int i = index; i < body.size(); i++) {
			Line content = body.get(i);
			content.bold = bold;
		}
		return this;
	}

	public String toHtml() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<html>");
		for (int i = 0; i < body.size(); i++) {
			Line content = body.get(i);

			if (content.bold) {
				stringBuffer.append("<b>");
			}
			if (content.color != -1) {
				stringBuffer.append("<font color = \"#" + String.format("%06X", (0xFFFFFF & content.color) + "\">"));
				stringBuffer.append(content.string);
				stringBuffer.append("</font>");
			}
			if (content.bold) {
				stringBuffer.append("</b>");
			}
			stringBuffer.append("</br>");
		}
		stringBuffer.append("</html>");
		return stringBuffer.toString();
	}

	private class Line {
		public String string;
		public int color = -1;
		public boolean bold = false;
	}
}
