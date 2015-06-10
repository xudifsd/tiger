package lexer;

import java.io.IOException;
import java.io.InputStream;

import util.Todo;
import lexer.Token.Kind;

public class Lexer {
	String fname; // the input file name to be compiled
	InputStream fstream; // input stream for the above file
	public int lineno;

	public Lexer(String fname, InputStream fstream) {
		this.fname = fname;
		this.fstream = fstream;
		this.lineno = 1;
	}

	// When called, return the next token (refer to the code "Token.java")
	// from the input stream.
	// Return TOKEN_EOF when reaching the end of the input stream.
	private Token nextTokenInternal() throws Exception {
		int c = this.fstream.read();
		if (-1 == c)
			// The value for "lineNum" is now "null",
			// you should modify this to an appropriate
			// line number for the "EOF" token.
			return new Token(Kind.TOKEN_EOF, this.lineno);

		// skip all kinds of "blanks"
		while (' ' == c || '\t' == c || '\n' == c) {
			if ('\n' == c)
				this.lineno++;
			c = this.fstream.read();
		}
		if (-1 == c)
			return new Token(Kind.TOKEN_EOF, this.lineno);

		switch (c) {
		case '+':
			return new Token(Kind.TOKEN_ADD, this.lineno);
		case '&':
			if (this.fstream.read() != '&')
				bug();
			else
				return new Token(Kind.TOKEN_AND, this.lineno);
		case '=':
			return new Token(Kind.TOKEN_ASSIGN, this.lineno);
		case 'b':
			if (expectFollowing("oolean"))
				return new Token(Kind.TOKEN_BOOLEAN, this.lineno);
			break;
		case 'c':
			if (expectFollowing("lass"))
				return new Token(Kind.TOKEN_CLASS, this.lineno);
			break;
		case ',':
			return new Token(Kind.TOKEN_COMMER, this.lineno);
		case '.':
			return new Token(Kind.TOKEN_DOT, this.lineno);
		case 'e':
			if (expectFollowing("lse"))
				return new Token(Kind.TOKEN_ELSE, this.lineno);
			else if (expectFollowing("xtends"))
				return new Token(Kind.TOKEN_EXTENDS, this.lineno);
			break;
		case 'f':
			if (expectFollowing("alse"))
				return new Token(Kind.TOKEN_FALSE, this.lineno);
			break;
		case 'i':
			if (expectFollowing("f"))
				return new Token(Kind.TOKEN_IF, this.lineno);
			else if (expectFollowing("nt"))
				return new Token(Kind.TOKEN_INT, this.lineno);
			break;
		case '{':
			return new Token(Kind.TOKEN_LBRACE, this.lineno);
		case '[':
			return new Token(Kind.TOKEN_LBRACK, this.lineno);
		case 'l':
			if (expectFollowing("ength"))
				return new Token(Kind.TOKEN_LENGTH, this.lineno);
			break;
		case '(':
			return new Token(Kind.TOKEN_LPAREN, this.lineno);
		case '<':
			return new Token(Kind.TOKEN_LT, this.lineno);
		case 'm':
			if (expectFollowing("ain"))
				return new Token(Kind.TOKEN_MAIN, this.lineno);
			break;
		case 'n':
			if (expectFollowing("ew"))
				return new Token(Kind.TOKEN_NEW, this.lineno);
			break;
		case '!':
			return new Token(Kind.TOKEN_NOT, this.lineno);
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return new Token(Kind.TOKEN_NUM, this.lineno, buildNum(c));
		case 'o':
			if (expectFollowing("ut"))
				return new Token(Kind.TOKEN_OUT, this.lineno);
			break;
		case 'p':
			if (expectFollowing("rintln"))
				return new Token(Kind.TOKEN_PRINTLN, this.lineno);
			else if (expectFollowing("ublic"))
				return new Token(Kind.TOKEN_PUBLIC, this.lineno);
			break;
		case '}':
			return new Token(Kind.TOKEN_RBRACE, this.lineno);
		case ']':
			return new Token(Kind.TOKEN_RBRACK, this.lineno);
		case 'r':
			if (expectFollowing("eturn"))
				return new Token(Kind.TOKEN_RETURN, this.lineno);
			break;
		case ')':
			return new Token(Kind.TOKEN_RPAREN, this.lineno);
		case ';':
			return new Token(Kind.TOKEN_SEMI, this.lineno);
		case 's':
			if (expectFollowing("tatic"))
				return new Token(Kind.TOKEN_STATIC, this.lineno);
			break;
		case 'S':
			if (expectFollowing("tring"))
				return new Token(Kind.TOKEN_STRING, this.lineno);
			else if (expectFollowing("ystem"))
				return new Token(Kind.TOKEN_SYSTEM, this.lineno);
			break;
		case '-':
			return new Token(Kind.TOKEN_SUB, this.lineno);
		case 't':
			if (expectFollowing("his"))
				return new Token(Kind.TOKEN_THIS, this.lineno);
			else if (expectFollowing("rue"))
				return new Token(Kind.TOKEN_TRUE, this.lineno);
			break;
		case '*':
			return new Token(Kind.TOKEN_TIMES, this.lineno);
		case 'v':
			if (expectFollowing("oid"))
				return new Token(Kind.TOKEN_VOID, this.lineno);
			break;
		case 'w':
			if (expectFollowing("hile"))
				return new Token(Kind.TOKEN_WHILE, this.lineno);
			break;
		case '/':// comment, FIXME we can't handle comment like /*sss*/
			dealComments(c);
			return nextTokenInternal();
		default:
			if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))//in [_a-zA-Z]
				break;
			else {
				// Lab 1, exercise 2: supply missing code to
				// lex other kinds of tokens.
				// Hint: think carefully about the basic
				// data structure and algorithms. The code
				// is not that much and may be less than 50 lines. If you
				// find you are writing a lot of code, you
				// are on the wrong way.
				new Todo();
				return null;
			}
		}
		return new Token(Kind.TOKEN_ID, this.lineno, buildId(c));
	}
	
	/**
	 * 
	 * @param c must be '/'
	 * @throws IOException
	 */
	private void dealComments(int c) throws IOException
	{
		//ex must be '/' or '*', otherwise, error.bug().
		int ex = this.fstream.read();
		if (ex == '/')
		{
			while (ex != '\n'&& ex!= -1)
			{
				this.fstream.mark(1);
				ex = this.fstream.read();
			}
			if (ex == -1)
			{
				this.fstream.reset();
				return;
			}
			else
				lineno++;
		}
		else if (ex == '*')
		{// '/*'must find a '*/'to mach, otherwise error. 
			ex = this.fstream.read();
			while ((c != '*' || ex != '/') && (ex != -1))
			{
				c = ex;
				ex = this.fstream.read();
			}
			if (ex == -1)
				util.Error.bug();
		}
		else
			util.Error.bug();
		// the else is well down
	}

	private boolean expectFollowing(String expectedString) throws IOException {
		this.fstream.mark(expectedString.length() + 1);
		for (int i = 0; i < expectedString.length(); i++) {
			if (expectedString.charAt(i) != this.fstream.read()) {
				this.fstream.reset();
				return false;
			}
		}
		this.fstream.mark(1);
		int c = this.fstream.read();
		this.fstream.reset();
		if (c != '_' && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')
				&& !(c >= '0' && c <= '9'))//not in [_a-zA-Z0-9]
			return true;
		return false;
	}

	private String buildId(int s) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append((char) s);
		for (;;) {
			this.fstream.mark(1);
			int c = this.fstream.read();
			if (c != '_' && !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')
					&& !(c >= '0' && c <= '9')) {//not in [_a-zA-Z0-9]
				this.fstream.reset();
				break;
			}
			sb.append((char) c);
		}
		return sb.toString();
	}

	private String buildNum(int s) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append((char) s);

		while (true)
		{
			this.fstream.mark(1);
			int c = this.fstream.read();
			if (c >= '0' &&c <= '9')
			{
				sb.append((char) c);
				continue;
			}

			// 999aaa is not a num.
			if ((c == '_') || (c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z'))
				util.Error.bug();

			break;
		}

		this.fstream.reset();
		return sb.toString();
	}

	private void bug() {
		System.err.format("in file %s, lineno %d, there are a bug", this.fname,
				this.lineno);
	}

	public Token nextToken() {
		Token t = null;

		try {
			t = this.nextTokenInternal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (control.Control.lex)
			System.out.println(t.toString());
		return t;
	}
}
