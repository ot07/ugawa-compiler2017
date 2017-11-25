import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.PiLangXParser.AddExprContext;
import parser.PiLangXParser.AndExprContext;
import parser.PiLangXParser.AssignStmtContext;
import parser.PiLangXParser.CallExprContext;
import parser.PiLangXParser.Cmp1ExprContext;
import parser.PiLangXParser.Cmp2ExprContext;
import parser.PiLangXParser.CompoundStmtContext;
import parser.PiLangXParser.ExprContext;
import parser.PiLangXParser.FuncDeclContext;
import parser.PiLangXParser.IfStmtContext;
import parser.PiLangXParser.LiteralExprContext;
import parser.PiLangXParser.MulExprContext;
import parser.PiLangXParser.OrExprContext;
import parser.PiLangXParser.ParenExprContext;
import parser.PiLangXParser.PrintStmtContext;
import parser.PiLangXParser.ProgContext;
import parser.PiLangXParser.ReturnStmtContext;
import parser.PiLangXParser.StmtContext;
import parser.PiLangXParser.UnaryOpExprContext;
import parser.PiLangXParser.VarExprContext;
import parser.PiLangXParser.WhileStmtContext;

public class ASTGenerator {	
	ASTFunctionNode translateFuncDecl(FuncDeclContext ctx) {
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<String> varDecls = new ArrayList<String>();
		ArrayList<ASTNode> stmts = new ArrayList<ASTNode>();
		String funcName = ctx.IDENTIFIER().getText();
		for (TerminalNode paramCtx: ctx.params().IDENTIFIER()) {
			String paramName = paramCtx.getText();
			params.add(paramName);
		}
		for (TerminalNode varCtx: ctx.varDecls().IDENTIFIER()) {
			String varName = varCtx.getText();
			varDecls.add(varName);
		}
		for (StmtContext stmtCtx: ctx.stmt()) {
			ASTNode stmt = translate(stmtCtx);
			stmts.add(stmt);
		}
		return new ASTFunctionNode(funcName, params, varDecls, stmts);
	}
	
	ASTNode translate(ParseTree ctxx) {
		if (ctxx instanceof ProgContext) {
			ProgContext ctx = (ProgContext) ctxx;
			ArrayList<String> varDecls = new ArrayList<String>();
			ArrayList<ASTFunctionNode> funcDecls = new ArrayList<ASTFunctionNode>();
			for (TerminalNode var: ctx.varDecls().IDENTIFIER()) {
				String varName = var.getText();
				varDecls.add(varName);
			}
			for (FuncDeclContext funcDeclCtx: ctx.funcDecl()) {
				ASTFunctionNode funcDecl = translateFuncDecl(funcDeclCtx);
				funcDecls.add(funcDecl);
			}
			return new ASTProgNode(varDecls, funcDecls);
		} else if (ctxx instanceof CompoundStmtContext) {
			CompoundStmtContext ctx = (CompoundStmtContext) ctxx;
			ArrayList<ASTNode> stmts = new ArrayList<ASTNode>();
			for (StmtContext t: ctx.stmt()) {
				ASTNode n = translate(t);
				stmts.add(n);
			}
			return new ASTCompoundStmtNode(stmts);
		} else if (ctxx instanceof AssignStmtContext) {
			AssignStmtContext ctx = (AssignStmtContext) ctxx;
			String var = ctx.IDENTIFIER().getText();
			ASTNode expr = translate(ctx.expr());
			return new ASTAssignStmtNode(var, expr);
		} else if (ctxx instanceof IfStmtContext) {
			IfStmtContext ctx = (IfStmtContext) ctxx;
			ASTNode cond = translate(ctx.expr());
			ASTNode thenClause = translate(ctx.stmt(0));
			ASTNode elseClause = translate(ctx.stmt(1));
			return new ASTIfStmtNode(cond, thenClause, elseClause);
		} else if (ctxx instanceof WhileStmtContext) {
			WhileStmtContext ctx = (WhileStmtContext) ctxx;
			ASTNode cond = translate(ctx.expr());
			ASTNode stmt = translate(ctx.stmt());
			return new ASTWhileStmtNode(cond, stmt);
		} else if (ctxx instanceof ReturnStmtContext) {
			ReturnStmtContext ctx = (ReturnStmtContext) ctxx;
			ASTNode expr = translate(ctx.expr());
			return new ASTReturnStmtNode(expr);
		} else if (ctxx instanceof PrintStmtContext) {
			PrintStmtContext ctx = (PrintStmtContext) ctxx;
			ASTNode expr = translate(ctx.expr());
			return new ASTPrintStmtNode(expr);
		} else if (ctxx instanceof ExprContext) {
			ExprContext ctx = (ExprContext) ctxx;
			return translate(ctx.orExpr());
		} else if (ctxx instanceof OrExprContext) {
			OrExprContext ctx = (OrExprContext) ctxx;
			if (ctx.orExpr() == null)
				return translate(ctx.andExpr());
			ASTNode lhs = translate(ctx.orExpr());
			ASTNode rhs = translate(ctx.andExpr());
			return new ASTBinaryExprNode(ctx.OROP().getText(), lhs, rhs);
		} else if (ctxx instanceof AndExprContext) {
			AndExprContext ctx = (AndExprContext) ctxx;
			if (ctx.andExpr() == null)
				return translate(ctx.cmp1Expr());
			ASTNode lhs = translate(ctx.andExpr());
			ASTNode rhs = translate(ctx.cmp1Expr());
			return new ASTBinaryExprNode(ctx.ANDOP().getText(), lhs, rhs);
		} else if (ctxx instanceof Cmp1ExprContext) {
			Cmp1ExprContext ctx = (Cmp1ExprContext) ctxx;
			if (ctx.cmp1Expr() == null)
				return translate(ctx.cmp2Expr());
			ASTNode lhs = translate(ctx.cmp1Expr());
			ASTNode rhs = translate(ctx.cmp2Expr());
			return new ASTBinaryExprNode(ctx.CMP1OP().getText(), lhs, rhs);
		} else if (ctxx instanceof Cmp2ExprContext) {
			Cmp2ExprContext ctx = (Cmp2ExprContext) ctxx;
			if (ctx.cmp2Expr() == null)
				return translate(ctx.addExpr());
			ASTNode lhs = translate(ctx.cmp2Expr());
			ASTNode rhs = translate(ctx.addExpr());
			return new ASTBinaryExprNode(ctx.CMP2OP().getText(), lhs, rhs);
		} else if (ctxx instanceof AddExprContext) {
			AddExprContext ctx = (AddExprContext) ctxx;
			if (ctx.addExpr() == null)
				return translate(ctx.mulExpr());
			ASTNode lhs = translate(ctx.addExpr());
			ASTNode rhs = translate(ctx.mulExpr());
			if (ctx.SUBOP() != null) {
				return new ASTBinaryExprNode(ctx.SUBOP().getText(), lhs, rhs);
			}
			return new ASTBinaryExprNode(ctx.ADDOP().getText(), lhs, rhs);
		} else if (ctxx instanceof MulExprContext) {
			MulExprContext ctx = (MulExprContext) ctxx;
			if (ctx.mulExpr() == null)
				return translate(ctx.unaryExpr());
			ASTNode lhs = translate(ctx.mulExpr());
			ASTNode rhs = translate(ctx.unaryExpr());
			return new ASTBinaryExprNode(ctx.MULOP().getText(), lhs, rhs);
		} else if (ctxx instanceof UnaryOpExprContext) {
			UnaryOpExprContext ctx = (UnaryOpExprContext) ctxx;
			ASTNode operand = translate(ctx.unaryExpr());
			if (ctx.SUBOP() != null) {
				return new ASTUnaryExprNode(ctx.SUBOP().getText(), operand);
			}
			return new ASTUnaryExprNode(ctx.NOTOP().getText(), operand);
		} else if (ctxx instanceof LiteralExprContext) {
			LiteralExprContext ctx = (LiteralExprContext) ctxx;
			int value = Integer.parseInt(ctx.VALUE().getText());
			return new ASTNumberNode(value);
		} else if (ctxx instanceof VarExprContext) {
			VarExprContext ctx = (VarExprContext) ctxx;
			String varName = ctx.IDENTIFIER().getText();
			return new ASTVarRefNode(varName);
		} else if (ctxx instanceof ParenExprContext) {
			ParenExprContext ctx = (ParenExprContext) ctxx;
			return translate(ctx.expr());
		} else if (ctxx instanceof CallExprContext) {
			CallExprContext ctx = (CallExprContext) ctxx;
			String funcName = ctx.IDENTIFIER().getText();
			ArrayList<ASTNode> args = new ArrayList<ASTNode>();
			for (ExprContext exprCtx: ctx.args().expr()) {
				ASTNode arg = translate(exprCtx);
				args.add(arg);
			}
			return new ASTCallNode(funcName, args);
		}
		throw new Error("Unknown parse tree node: "+ctxx.getText());		
	}
}
