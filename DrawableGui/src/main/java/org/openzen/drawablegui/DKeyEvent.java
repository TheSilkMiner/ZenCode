/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui;

/**
 * @author Hoofdgebruiker
 */
public class DKeyEvent {
	public static final char CHAR_UNDEFINED = 65535;

	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 4;
	public static final int ALT = 256;
	public static final int CTRL = 512;
	public static final int SHIFT = 1024;
	public static final int META = 2048;
	public static final int ALT_GRAPH = 4096;

	public final char character;
	public final KeyCode keyCode;
	public final int modifiers;

	public DKeyEvent(char character, KeyCode keyCode, int modifiers) {
		this.character = character;
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}

	public boolean has(int modifiers) {
		return (this.modifiers & modifiers) == modifiers;
	}

	public enum KeyCode {
		ENTER,
		BACKSPACE,
		TAB,
		CANCEL,
		CLEAR,
		SHIFT,
		CONTROL,
		ALT,
		PAUSE,
		CAPS_LOCK,
		ESCAPE,
		SPACE,
		PAGE_UP,
		PAGE_DOWN,
		END,
		HOME,
		LEFT,
		UP,
		RIGHT,
		DOWN,
		COMMA,
		MINUS,
		PERIOD,
		SLASH,
		NUM0,
		NUM1,
		NUM2,
		NUM3,
		NUM4,
		NUM5,
		NUM6,
		NUM7,
		NUM8,
		NUM9,
		SEMICOLON,
		EQUALS,
		A,
		B,
		C,
		D,
		E,
		F,
		G,
		H,
		I,
		J,
		K,
		L,
		M,
		N,
		O,
		P,
		Q,
		R,
		S,
		T,
		U,
		V,
		W,
		X,
		Y,
		Z,
		OPEN_BRACKET,
		BACKSLASH,
		CLOSE_BRACKET,
		NUMPAD0,
		NUMPAD1,
		NUMPAD2,
		NUMPAD3,
		NUMPAD4,
		NUMPAD5,
		NUMPAD6,
		NUMPAD7,
		NUMPAD8,
		NUMPAD9,
		MULTIPLY,
		ADD,
		SEPARATOR,
		SUBTRACT,
		DECIMAL,
		DIVIDE,
		DELETE,
		NUM_LOCK,
		SCROLL_LOCK,
		F1,
		F2,
		F3,
		F4,
		F5,
		F6,
		F7,
		F8,
		F9,
		F10,
		F11,
		F12,
		F13,
		F14,
		F15,
		F16,
		F17,
		F18,
		F19,
		F20,
		F21,
		F22,
		F23,
		F24,
		PRINTSCREEN,
		INSERT,
		HELP,
		META,
		BACKQUOTE,
		QUOTE,
		KEYPAD_UP,
		KEYPAD_DOWN,
		KEYPAD_LEFT,
		KEYPAD_RIGHT,
		AMPERSAND,
		ASTERISK,
		QUOTEDBL,
		LESS,
		GREATER,
		BRACELEFT,
		BRACERIGHT,
		AT,
		COLON,
		CIRCUMFLEX,
		DOLLAR,
		EURO_SIGN,
		EXCLAMATION_MARK,
		INVERTED_EXCLAMATION_MARK,
		LEFT_PARENTHESIS,
		NUMBER_SIGN,
		PLUS,
		RIGHT_PARENTHESIS,
		UNDERSCORE,
		WINDOWS,
		CONTEXT_MENU,
		FINAL,
		CONVERT,
		NONCONVERT,
		ACCEPT,
		KANA,
		KANJI,
		ALPHANUMERIC,
		KATAKANA,
		HIRAGANA,
		FULL_WIDTH,
		HALF_WIDTH,
		ROMAN_CHARACTERS,
		ALL_CANDIDATES,
		PREVIOUS_CANDIDATE,
		CODE_INPUT,
		JAPANESE_KATAKANA,
		JAPANESE_HIRAGANA,
		JAPANESE_ROMAN,
		KANA_LOCK,
		INPUT_METHOD_ON_OFF,
		CUT,
		COPY,
		PASTE,
		UNDO,
		AGAIN,
		FIND,
		PROPS,
		STOP,
		COMPOSE,
		ALT_GRAPH,
		BEGIN,
		UNKNOWN
	}
}
