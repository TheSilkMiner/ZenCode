package org.openzen.zenscript.ide.ui.view.editor;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.lexer.CharReader;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.TokenParser;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;

public class TokenRelexer {
	private final List<TokenLine> lines;
	private final SourceFile file;
	private final int toLine;
	private final int toToken;

	private int lineIndex;
	private int token;

	public TokenRelexer(SourceFile file, List<TokenLine> lines, int fromLine, int fromToken, int toLine, int toToken) {
		if (fromToken < 0 || toToken < 0)
			throw new IllegalArgumentException("fromToken or toToken cannot be < 0");

		this.lines = lines;
		this.file = file;
		this.toLine = toLine;
		this.toToken = toToken;

		this.lineIndex = fromLine;
		this.token = fromToken;
	}

	public List<ZSToken> relex() throws ParseException {
		RelexCharReader reader = new RelexCharReader();
		TokenParser<ZSToken, ZSTokenType> reparser = ZSTokenParser.createRaw(file, reader);
		List<ZSToken> result = new ArrayList<>();
		while ((lineIndex < toLine || token < toToken || !reader.isAtTokenBoundary()) && reparser.hasNext())
			result.add(reparser.next());
		return result;
	}

	private String get() {
		TokenLine line = lines.get(lineIndex);
		if (token == line.getTokenCount())
			return "\n"; // special "newline" token for transitions to next line

		return line.getToken(token).content;
	}

	private boolean advance() {
		if (lineIndex == lines.size() - 1 && token == lines.get(lineIndex).getTokenCount())
			return false;

		token++;
		if (token > lines.get(lineIndex).getTokenCount()) {
			lineIndex++;
			token = 0;
		}
		return true;
	}

	public int getLine() {
		return lineIndex;
	}

	public int getToken() {
		return token;
	}

	private class RelexCharReader implements CharReader {
		private String token;
		private int tokenOffset;

		public RelexCharReader() {
			token = get();
		}

		public boolean isAtTokenBoundary() {
			return tokenOffset == 0;
		}

		@Override
		public int peek() {
			return token == null ? -1 : token.charAt(tokenOffset);
		}

		@Override
		public int next() {
			int result = peek();
			tokenOffset++;
			if (tokenOffset == token.length()) {
				if (advance()) {
					token = get();
					tokenOffset = 0;
				} else {
					token = null;
				}
			}
			return result;
		}
	}
}
