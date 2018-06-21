// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package scala.meta.internal.semanticdb

@SerialVersionUID(0L)
final case class SymbolOccurrence(
    range: _root_.scala.Option[scala.meta.internal.semanticdb.Range] = None,
    symbol: _root_.scala.Predef.String = "",
    role: scala.meta.internal.semanticdb.SymbolOccurrence.Role = scala.meta.internal.semanticdb.SymbolOccurrence.Role.UNKNOWN_ROLE
    ) extends scalapb.GeneratedMessage with scalapb.Message[SymbolOccurrence] with scalapb.lenses.Updatable[SymbolOccurrence] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      if (range.isDefined) {
        val __value = range.get
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      };
      
      {
        val __value = symbol
        if (__value != "") {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
        }
      };
      
      {
        val __value = role
        if (__value != scala.meta.internal.semanticdb.SymbolOccurrence.Role.UNKNOWN_ROLE) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(3, __value.value)
        }
      };
      __size
    }
    final override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      range.foreach { __v =>
        val __m = __v
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      {
        val __v = symbol
        if (__v != "") {
          _output__.writeString(2, __v)
        }
      };
      {
        val __v = role
        if (__v != scala.meta.internal.semanticdb.SymbolOccurrence.Role.UNKNOWN_ROLE) {
          _output__.writeEnum(3, __v.value)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): scala.meta.internal.semanticdb.SymbolOccurrence = {
      var __range = this.range
      var __symbol = this.symbol
      var __role = this.role
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __range = Option(_root_.scalapb.LiteParser.readMessage(_input__, __range.getOrElse(scala.meta.internal.semanticdb.Range.defaultInstance)))
          case 18 =>
            __symbol = _input__.readString()
          case 24 =>
            __role = scala.meta.internal.semanticdb.SymbolOccurrence.Role.fromValue(_input__.readEnum())
          case tag => _input__.skipField(tag)
        }
      }
      scala.meta.internal.semanticdb.SymbolOccurrence(
          range = __range,
          symbol = __symbol,
          role = __role
      )
    }
    def getRange: scala.meta.internal.semanticdb.Range = range.getOrElse(scala.meta.internal.semanticdb.Range.defaultInstance)
    def clearRange: SymbolOccurrence = copy(range = None)
    def withRange(__v: scala.meta.internal.semanticdb.Range): SymbolOccurrence = copy(range = Option(__v))
    def withSymbol(__v: _root_.scala.Predef.String): SymbolOccurrence = copy(symbol = __v)
    def withRole(__v: scala.meta.internal.semanticdb.SymbolOccurrence.Role): SymbolOccurrence = copy(role = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => range.orNull
        case 2 => {
          val __t = symbol
          if (__t != "") __t else null
        }
        case 3 => {
          val __t = role.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => range.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 2 => _root_.scalapb.descriptors.PString(symbol)
        case 3 => _root_.scalapb.descriptors.PEnum(role.scalaValueDescriptor)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = scala.meta.internal.semanticdb.SymbolOccurrence
}

object SymbolOccurrence extends scalapb.GeneratedMessageCompanion[scala.meta.internal.semanticdb.SymbolOccurrence] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[scala.meta.internal.semanticdb.SymbolOccurrence] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, _root_.scala.Any]): scala.meta.internal.semanticdb.SymbolOccurrence = {
    require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    scala.meta.internal.semanticdb.SymbolOccurrence(
      __fieldsMap.get(__fields.get(0)).asInstanceOf[_root_.scala.Option[scala.meta.internal.semanticdb.Range]],
      __fieldsMap.getOrElse(__fields.get(1), "").asInstanceOf[_root_.scala.Predef.String],
      scala.meta.internal.semanticdb.SymbolOccurrence.Role.fromValue(__fieldsMap.getOrElse(__fields.get(2), scala.meta.internal.semanticdb.SymbolOccurrence.Role.UNKNOWN_ROLE.javaValueDescriptor).asInstanceOf[_root_.com.google.protobuf.Descriptors.EnumValueDescriptor].getNumber)
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[scala.meta.internal.semanticdb.SymbolOccurrence] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      scala.meta.internal.semanticdb.SymbolOccurrence(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[_root_.scala.Option[scala.meta.internal.semanticdb.Range]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        scala.meta.internal.semanticdb.SymbolOccurrence.Role.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(scala.meta.internal.semanticdb.SymbolOccurrence.Role.UNKNOWN_ROLE.scalaValueDescriptor).number)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = SemanticdbProto.javaDescriptor.getMessageTypes.get(25)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = SemanticdbProto.scalaDescriptor.messages(25)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = scala.meta.internal.semanticdb.Range
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 3 => scala.meta.internal.semanticdb.SymbolOccurrence.Role
    }
  }
  lazy val defaultInstance = scala.meta.internal.semanticdb.SymbolOccurrence(
  )
  sealed trait Role extends _root_.scalapb.GeneratedEnum {
    type EnumType = Role
    def isUnknownRole: _root_.scala.Boolean = false
    def isReference: _root_.scala.Boolean = false
    def isDefinition: _root_.scala.Boolean = false
    def companion: _root_.scalapb.GeneratedEnumCompanion[Role] = scala.meta.internal.semanticdb.SymbolOccurrence.Role
  }
  
  object Role extends _root_.scalapb.GeneratedEnumCompanion[Role] {
    implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[Role] = this
    @SerialVersionUID(0L)
    case object UNKNOWN_ROLE extends Role {
      val value = 0
      val index = 0
      val name = "UNKNOWN_ROLE"
      override def isUnknownRole: _root_.scala.Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object REFERENCE extends Role {
      val value = 1
      val index = 1
      val name = "REFERENCE"
      override def isReference: _root_.scala.Boolean = true
    }
    
    @SerialVersionUID(0L)
    case object DEFINITION extends Role {
      val value = 2
      val index = 2
      val name = "DEFINITION"
      override def isDefinition: _root_.scala.Boolean = true
    }
    
    @SerialVersionUID(0L)
    final case class Unrecognized(value: _root_.scala.Int) extends Role with _root_.scalapb.UnrecognizedEnum
    
    lazy val values = scala.collection.Seq(UNKNOWN_ROLE, REFERENCE, DEFINITION)
    def fromValue(value: _root_.scala.Int): Role = value match {
      case 0 => UNKNOWN_ROLE
      case 1 => REFERENCE
      case 2 => DEFINITION
      case __other => Unrecognized(__other)
    }
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = scala.meta.internal.semanticdb.SymbolOccurrence.javaDescriptor.getEnumTypes.get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = scala.meta.internal.semanticdb.SymbolOccurrence.scalaDescriptor.enums(0)
  }
  implicit class SymbolOccurrenceLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, scala.meta.internal.semanticdb.SymbolOccurrence]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, scala.meta.internal.semanticdb.SymbolOccurrence](_l) {
    def range: _root_.scalapb.lenses.Lens[UpperPB, scala.meta.internal.semanticdb.Range] = field(_.getRange)((c_, f_) => c_.copy(range = Option(f_)))
    def optionalRange: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[scala.meta.internal.semanticdb.Range]] = field(_.range)((c_, f_) => c_.copy(range = f_))
    def symbol: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.symbol)((c_, f_) => c_.copy(symbol = f_))
    def role: _root_.scalapb.lenses.Lens[UpperPB, scala.meta.internal.semanticdb.SymbolOccurrence.Role] = field(_.role)((c_, f_) => c_.copy(role = f_))
  }
  final val RANGE_FIELD_NUMBER = 1
  final val SYMBOL_FIELD_NUMBER = 2
  final val ROLE_FIELD_NUMBER = 3
}
