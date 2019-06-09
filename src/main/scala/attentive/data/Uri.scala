package attentive.data

import fastparse._
import NoWhitespace._

final case class Uri(protocol: Option[String], host: String, port: Option[Int], user: Option[String], path: Seq[String]) {

  def asString: String =
    Uri.ToString(this).asString

  def hostAndPort: String = {
    val s = Uri.ToString(this)
    s.host + s.port
  }

  def setPort(p: Int): Uri =
    if (port.contains(p)) this
    else copy(port = Some(p))

  def withoutPort: Uri =
    if (port.isEmpty) this
    else copy(port = None)

  def fallbackPort(p: Int): Uri =
    if (port.isEmpty) setPort(p)
    else this

  def setProtocol(proto: String): Uri =
    if (protocol.contains(proto)) this
    else copy(protocol = Some(proto))

  def withoutProtocol: Uri =
    if (protocol.isEmpty) this
    else copy(protocol = None)

  def fallbackProtocol(proto: String): Uri =
    if (protocol.isEmpty) setProtocol(proto)
    else this

  def setHost(h: String): Uri =
    if (host == h) this else copy(host = h)

  def setUser(u: String): Uri =
    if (user.contains(u)) this else copy(user = Some(u))

  def withoutUser: Uri =
    if (user.isEmpty) this else copy(user = None)

  def fallbackUser(u: String): Uri =
    if (user.isEmpty) setUser(u) else this

  def /(seg: String): Uri =
    copy(path = path :+ seg)
}

object Uri {

  def parse(s: String): Either[String, Uri] = fastparse.parse(s, Parser.uri(_)) match {
    case Parsed.Success(c, _) => Right(c)
    case f@Parsed.Failure(_, _, _) =>
      val trace = f.trace()
      Left(s"Cannot parse uri '$s' at index ${trace.index}: ${trace.msg}")
  }

  private object Parser {

    def proto[_:P] = P(CharIn("a-z").rep(1)).!

    def user[_: P] = P(CharIn("a-b") ~ CharPred(c => c != '@').rep).!

    def host[_: P] = P(CharPred(c => c != ':').rep).!

    def port[_: P] = P(CharIn("0-9").rep(1).!).map(_.toInt)

    def seg[_: P]: P[String] = P(CharPred(c => c != '/').rep.!)

    def path[_: P]: P[Seq[String]] = P(seg.rep(sep = "/"))

    def uri[_: P] = P((proto ~ "://").? ~ (user ~ "@").? ~ host ~ (":" ~ port).? ~ ("/" ~ path).?).map {
      case (pr, u, h, po, pa) =>
        Uri(pr, h, po, u, pa.getOrElse(Seq.empty).map(PercentEncoding.decode))
    }
  }

  case class ToString(u: Uri) {

    def proto = u.protocol.map(p => p + "://").getOrElse("")

    def user = u.user.map(u => u + "@").getOrElse("")

    def host = u.host

    def port = u.port.map(p => ":" + p).getOrElse("")

    def path =
      if (u.path.isEmpty) ""
      else "/" + u.path.map(PercentEncoding.encode).mkString("/")

    def asString =
      proto + user + host + port + path
  }

  object PercentEncoding {
    val valid = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ "-._~$+;%").toSet

    def encode(s: String): String =
      s.flatMap { c =>
        if (valid.contains(c)) c.toString
        else "%" + c.toInt.toHexString
      }

    def decode(s: String): String =
      Decoder.tokenize(s) match {
        case Left(_) => s
        case Right(tokens) =>
          tokens.map({
            case Decoder.Part.Rwpt(e) => e
            case Decoder.Part.Pcpt(e) =>
              val hex = e.substring(1)
              Integer.parseInt(hex, 16).toChar.toString
          }).mkString
      }

    object Decoder {
      sealed trait Part
      object Part {
        case class Pcpt(s: String) extends Part
        case class Rwpt(s: String) extends Part
      }

      def pcpt[_: P]: P[Part] =
        P("%" ~ CharIn("0-9a-f").rep(exactly = 2)).!.map(s => Part.Pcpt(s))
      def rest[_: P]: P[Part] =
        P(!"%" ~ AnyChar).rep(1).!.map(s => Part.Rwpt(s))

      def dec[_: P]: P[Seq[Part]] = P(rest | pcpt).rep

      def tokenize(s: String): Either[String, Seq[Part]] = fastparse.parse(s, dec(_)) match {
        case Parsed.Success(c, _) => Right(c)
        case f@Parsed.Failure(_, _, _) => Left(s)
      }
    }
  }
}
