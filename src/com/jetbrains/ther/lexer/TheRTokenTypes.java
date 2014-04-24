package com.jetbrains.ther.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.ther.psi.TheRElementType;

public class TheRTokenTypes {
  /*
   * There are five types of constants: integer, logical, numeric, complex and string.
   */

  // integer constants
  public static final IElementType INTEGER_LITERAL = new TheRElementType("INTEGER_LITERAL");

  // logical constants
  public static final IElementType TRUE_KEYWORD = new TheRElementType("TRUE_KEYWORD");
  public static final IElementType FALSE_KEYWORD = new TheRElementType("FALSE_KEYWORD");

  // numeric constants
  public static final IElementType NUMERIC_LITERAL = new TheRElementType("NUMERIC_LITERAL");

  // complex constants
  public static final IElementType COMPLEX_LITERAL = new TheRElementType("COMPLEX_LITERAL");

  // string constants
  public static final IElementType STRING_LITERAL = new TheRElementType("STRING_LITERAL");

  /*
   *  In addition, there are four special constants
   */
  public static final IElementType NULL_KEYWORD = new TheRElementType("NULL_KEYWORD");       // empty object
  public static final IElementType INF_KEYWORD = new TheRElementType("INF_KEYWORD");         // infinity
  public static final IElementType NAN_KEYWORD = new TheRElementType("NAN_KEYWORD");        // not-a-number in the IEEE floating point calculus (results of the operations respectively 1/0 and 0/0, for instance).
  public static final IElementType NA_KEYWORD = new TheRElementType("NA_KEYWORD");          // absent (“Not Available”) data values



  /*
  The following identifiers have a special meaning and cannot be used for object names
  if else repeat while function for in next break
  TRUE FALSE NULL Inf NaN
  NA NA_integer_ NA_real_ NA_complex_ NA_character_
  ... ..1 ..2 etc.
   */
  // keywords

  public static final IElementType NA_INTEGER_KEYWORD = new TheRElementType("NA_INTEGER_KEYWORD");
  public static final IElementType NA_REAL_KEYWORD = new TheRElementType("NA_REAL_KEYWORD");
  public static final IElementType NA_COMPLEX_KEYWORD = new TheRElementType("NA_COMPLEX_KEYWORD");
  public static final IElementType NA_CHARACTER_KEYWORD = new TheRElementType("NA_CHARACTER_KEYWORD");

  public static final IElementType TRIPLE_DOTS = new TheRElementType("TRIPLE_DOTS");

  public static final IElementType IF_KEYWORD = new TheRElementType("IF_KEYWORD");
  public static final IElementType ELSE_KEYWORD = new TheRElementType("ELSE_KEYWORD");
  public static final IElementType REPEAT_KEYWORD = new TheRElementType("REPEAT_KEYWORD");
  public static final IElementType WHILE_KEYWORD = new TheRElementType("WHILE_KEYWORD");
  public static final IElementType FUNCTION_KEYWORD = new TheRElementType("FUNCTION_KEYWORD");
  public static final IElementType FOR_KEYWORD = new TheRElementType("FOR_KEYWORD");
  public static final IElementType IN_KEYWORD = new TheRElementType("IN_KEYWORD");
  public static final IElementType NEXT_KEYWORD = new TheRElementType("NEXT_KEYWORD");
  public static final IElementType BREAK_KEYWORD = new TheRElementType("BREAK_KEYWORD");

  /*
  R contains a number of operators. They are listed in the table below.
  - Minus, can be unary or binary
  + Plus, can be unary or binary
  ! Unary not
  ~ Tilde, used for model formulae, can be either unary or binary
  ? Help
  : Sequence, binary (in model formulae: interaction)
  * Multiplication, binary
  / Division, binary
  ^ Exponentiation, binary
  %x% Special binary operators, x can be replaced by any valid name
  %% Modulus, binary
  %/% Integer divide, binary
  %*% Matrix product, binary
  %o% Outer product, binary
  %x% Kronecker product, binary
  %in% Matching operator, binary (in model formulae: nesting)
  < Less than, binary
  > Greater than, binary
  == Equal to, binary
  >= Greater than or equal to, binaryChapter 3: Evaluation of expressions 11
  <= Less than or equal to, binary
  & And, binary, vectorized
  && And, binary, not vectorized
  | Or, binary, vectorized
  || Or, binary, not vectorized
  <- Left assignment, binary
  -> Right assignment, binary
  $ List subset, binary
  */

  public static final IElementType MINUS = new TheRElementType("MINUS");  // -
  public static final IElementType PLUS = new TheRElementType("PLUS");    // +
  public static final IElementType NOT = new TheRElementType("NOT");      // !
  public static final IElementType TILDE = new TheRElementType("TILDE");  // ~
  public static final IElementType HELP = new TheRElementType("HELP");    // ?
  public static final IElementType COLON = new TheRElementType("COLON");  // :
  public static final IElementType MULT = new TheRElementType("MULT");    // *
  public static final IElementType DIV = new TheRElementType("DIV");      // /
  public static final IElementType EXP = new TheRElementType("EXP");      // ^
  public static final IElementType MODULUS = new TheRElementType("MODULUS");  // %%
  public static final IElementType INT_DIV = new TheRElementType("INT_DIV");  // %/%
  public static final IElementType MATRIX_PROD = new TheRElementType("MATRIX_PROD");  // %*%
  public static final IElementType OUTER_PROD = new TheRElementType("OUTER_PROD");    // %o%
  public static final IElementType MATCHING = new TheRElementType("MATCHING");        // %in%
  public static final IElementType KRONECKER_PROD = new TheRElementType("MATCHING");        // %x%
  public static final IElementType INFIX_OP = new TheRElementType("INFIX_OP");        // %{character}+%
  public static final IElementType LT = new TheRElementType("LT");        // <
  public static final IElementType GT = new TheRElementType("GT");        // >
  public static final IElementType EQEQ = new TheRElementType("EQEQ");    // ==
  public static final IElementType GTEQ = new TheRElementType("GTEQ");    // >=
  public static final IElementType LTEQ = new TheRElementType("LTEQ");    // <=
  public static final IElementType AND = new TheRElementType("AND");      // &
  public static final IElementType ANDAND = new TheRElementType("ANDAND");  // &&
  public static final IElementType OR = new TheRElementType("OR");        // |
  public static final IElementType OROR = new TheRElementType("OROR");    // ||
  public static final IElementType LEFT_ASSIGN = new TheRElementType("LEFT_ASSIGN");    // <-
  public static final IElementType RIGHT_ASSIGN = new TheRElementType("RIGHT_ASSIGN");  // ->
  public static final IElementType LIST_SUBSET = new TheRElementType("LIST_SUBSET");    // $

  public static final IElementType AT = new TheRElementType("AT");    // @
  public static final IElementType COLONCOLON = new TheRElementType("COLONCOLON");    // ::
  public static final IElementType NOTEQ = new TheRElementType("NOTEQ");    // !=
  public static final IElementType RIGHT_COMPLEX_ASSING = new TheRElementType("RIGHT_COMPLEX_ASSING");    // ->>
  public static final IElementType LEFT_COMPLEX_ASSING = new TheRElementType("LEFT_COMPLEX_ASSING");    // <<-

  public static final IElementType LPAR = new TheRElementType("LPAR");    // (
  public static final IElementType RPAR = new TheRElementType("RPAR");    // )
  public static final IElementType LBRACKET = new TheRElementType("LBRACKET");    // [
  public static final IElementType LDBRACKET = new TheRElementType("LBRACKET");    // [[
  public static final IElementType RBRACKET = new TheRElementType("RBRACKET");    // ]
  public static final IElementType RDBRACKET = new TheRElementType("RBRACKET");    // ]]
  public static final IElementType LBRACE = new TheRElementType("LBRACE");    // {
  public static final IElementType RBRACE = new TheRElementType("RBRACE");    // }
  public static final IElementType COMMA = new TheRElementType("COMMA");    // ,
  public static final IElementType DOT = new TheRElementType("DOT");    // .
  public static final IElementType EQ = new TheRElementType("EQ");    // =
  public static final IElementType SEMICOLON = new TheRElementType("SEMICOLON");    // ;

  public static final IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

  public static final IElementType IDENTIFIER = new TheRElementType("IDENTIFIER");
  public static final IElementType LINE_BREAK = new TheRElementType("LINE_BREAK");
  public static final IElementType SPACE = new TheRElementType("SPACE");
  public static final IElementType TAB = new TheRElementType("TAB");

  public static final IElementType END_OF_LINE_COMMENT = new TheRElementType("END_OF_LINE_COMMENT");


}
