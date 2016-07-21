package literaltypes

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.InfoTransform
import nsc.transform.TypingTransformers
import nsc.symtab.Flags._
import nsc.ast.TreeDSL
import nsc.typechecker

import scala.reflect.NameTransformer
import scala.collection.mutable

class LiteralTypes(val global: Global) extends Plugin {
  val name = "symbol-types"
  val description = "Permit symbols to be directly used in type position to refer to their singleton types"
  val components = new LiteralTypeRewriter(this, global) :: Nil
}

class LiteralTypeRewriter(plugin: Plugin, val global: Global)
    extends PluginComponent with Transform with TypingTransformers with TreeDSL {

  import global._
  import global.internal.constantType

  val runsAfter = "parser" :: Nil
  val phaseName = "symbol-types"
  override val runsRightAfter: Option[String] = Some("parser")

  def newTransformer(unit: CompilationUnit): LiteralTypeTransformer = {
    new LiteralTypeTransformer(unit)
  }

  class LiteralTypeTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {

    // Thanks to kind-projector!
    val treeCache = mutable.Map.empty[Tree, Tree]

    def transformIdentToType(tree: Tree): Tree = tree match {
      // Symbol literal - since symbols aren't actually constants, this has to use Shapeless' strategy
      // which means Shapeless is assumed to be available
      case Ident(name) if name.decodedName.startsWith('\'') =>
        val singleName = name.decodedName.toString.stripPrefix("'")
        val out = AppliedTypeTree(
          Select(Select(Ident("shapeless"), "tag"), TypeName("$at$at")),
          List(TypeTree(typeOf[scala.Symbol]), TypeTree(constantType(Constant(singleName)))))
        atPos(tree.pos.makeTransparent)(out)

      // String literals
      // TODO - this should probably be smarter about parsing the string literal
      case Ident(name) if name.decodedName.startsWith('"') && name.decodedName.endsWith('"') =>
        val singleName = name.decodedName.toString.stripPrefix("\"").stripSuffix("\"")
        TypeTree(constantType(Constant(singleName)))

      // Integer literals
      case Ident(name) if name.decodedName.toString.headOption.exists(_.isDigit) => try {
        val singleInt = name.decodedName.toString.toInt
        TypeTree(constantType(Constant(singleInt)))
      } catch {
        case err: Throwable => tree
      }

      case other => other
    }

    def rewriteableTypeName(name: Name) = name.decodedName.startsWith('\'') ||
      (name.decodedName.startsWith('"') && name.decodedName.endsWith('"')) ||
      name.decodedName.toString.headOption.exists(_.isDigit)

    def rewriteTypeDefSubtree(tree: Tree): Tree = tree match {
      case t@AppliedTypeTree(f, args) => t.copy(args = args.map(rewriteTypeDefSubtree))
      case t@TypeDef(_, _, _, rhs) => t.copy(rhs = rewriteTypeDefSubtree(rhs))
      case i@Ident(name) if rewriteableTypeName(name) => transformIdentToType(i)
      case other => other
    }

    // we only want to do this for identifiers in a type position
    def transformTypes(tree: Tree) = {
      val pos = tree.pos.makeTransparent
      tree match {
        case t@AppliedTypeTree(f, args) => atPos(pos)(t.copy(args = args.map(transformIdentToType)))
        case v@ValDef(_, _, tpt, _) => atPos(pos)(v.copy(tpt = transformIdentToType(tpt)))
        case t@TypeDef(_, _, _, rhs) => atPos(pos)(t.copy(rhs = rewriteTypeDefSubtree(rhs)))
        case _ =>
          super.transform(tree)
      }
    }

    override def transform(tree: Tree): Tree = {
      treeCache.get(tree) match {
        case Some(result) => result
        case None =>
          val result = transformTypes(tree)
          treeCache(tree) = result
          result
      }
    }
  }
}
