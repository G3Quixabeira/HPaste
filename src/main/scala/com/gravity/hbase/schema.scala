/** Licensed to Gravity.com under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. Gravity.com licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.gravity.hbase

import org.apache.hadoop.hbase.util.Bytes
import scala.collection._
import org.joda.time.{DateMidnight, DateTime}
import org.apache.hadoop.io.BytesWritable
import java.io._

import scala.util.matching.Regex

/*             )\._.,--....,'``.
.b--.        /;   _.. \   _\  (`._ ,.
`=,-,-'~~~   `----(,_..'--(,_..'`-.;.'  */

class AnyNotSupportedException() extends Exception("Any not supported")

trait AnyConverterSignal

/**
  * This is the standard set of types that can be auto converted into hbase values (they work as families, columns, and values)
  */
package object schema {

  def toBytesWritable[T](item: T)(implicit c: ByteConverter[T]): BytesWritable = {
    c.toBytesWritable(item)
  }

  def fromBytesWritable[T](bytes: BytesWritable)(implicit c: ByteConverter[T]): T = {
    c.fromBytesWritable(bytes)
  }


  type FamilyExtractor[T <: HbaseTable[T, _, _]] = (T) => ColumnFamily[T, _, _, _, _]
  type ColumnExtractor[T <: HbaseTable[T, _, _]] = (T) => Column[T, _, _, _, _]

  implicit object AnyConverter extends ByteConverter[Any] with AnyConverterSignal {
    override def toBytes(t: Any): Nothing = throw new AnyNotSupportedException()

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Nothing = throw new AnyNotSupportedException()
  }

  implicit object StringConverter extends ByteConverter[String] {
    override def toBytes(t: String): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): String = Bytes.toString(bytes, offset, length)
  }

  implicit object StringSeqConverter extends SeqConverter[String]

  implicit object StringSetConverter extends SetConverter[String]


  implicit object IntConverter extends ByteConverter[Int] {
    override def toBytes(t: Int): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Int = Bytes.toInt(bytes, offset, length)
  }

  implicit object IntSeqConverter extends SeqConverter[Int]

  implicit object IntSetConverter extends SetConverter[Int]

  implicit object ShortConverter extends ByteConverter[Short] {
    override def toBytes(t: Short): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Short = Bytes.toShort(bytes, offset, length)
  }

  implicit object ShortSeqConverter extends SeqConverter[Short]

  implicit object ShortSetConverter extends SetConverter[Short]

  implicit object BooleanConverter extends ByteConverter[Boolean] {
    override def toBytes(t: Boolean): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Boolean = {
      (bytes(offset) != 0)
    }
  }

  implicit object BooleanSeqConverter extends SeqConverter[Boolean]

  implicit object BooleanSetConverter extends SetConverter[Boolean]

  implicit object LongConverter extends ByteConverter[Long] {
    override def toBytes(t: Long): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Long = Bytes.toLong(bytes, offset, length)
  }

  implicit object LongSeqConverter extends SeqConverter[Long]

  implicit object LongSetConverter extends SetConverter[Long]

  implicit object DoubleConverter extends ByteConverter[Double] {
    override def toBytes(t: Double): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Double = Bytes.toDouble(bytes, offset)
  }

  implicit object DoubleSeqConverter extends SeqConverter[Double]

  implicit object DoubleSetConverter extends SetConverter[Double]


  implicit object FloatConverter extends ByteConverter[Float] {
    override def toBytes(t: Float): Array[Byte] = Bytes.toBytes(t)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): Float = Bytes.toFloat(bytes, offset)
  }

  implicit object FloatSeqConverter extends SeqConverter[Float]

  implicit object FloatSetConverter extends SetConverter[Float]


  implicit object CommaSetConverter extends ByteConverter[CommaSet] {
    val SPLITTER: Regex = ",".r

    override def toBytes(t: CommaSet): Array[Byte] = Bytes.toBytes(t.items.mkString(","))

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): CommaSet = new CommaSet(SPLITTER.split(Bytes.toString(bytes, offset, length)).toSet)
  }

  implicit object CommaSetSeqConverter extends SeqConverter[CommaSet]

  implicit object CommaSetSetConverter extends SetConverter[CommaSet]


  implicit object YearDayConverter extends ByteConverter[YearDay] {
    val SPLITTER: Regex = "_".r

    override def toBytes(t: YearDay): Array[Byte] = Bytes.toBytes(t.year.toString + "_" + t.day.toString)

    override def fromBytes(bytes: Array[Byte], offset: Int, length: Int): YearDay = {
      val strRep = Bytes.toString(bytes, offset, length)
      val strRepSpl = SPLITTER.split(strRep)
      val year = strRepSpl(0).toInt
      val day = strRepSpl(1).toInt
      YearDay(year, day)
    }
  }

  implicit object YearDaySeqConverter extends SeqConverter[YearDay]

  implicit object YearDaySetConverter extends SetConverter[YearDay]

  implicit object DateMidnightConverter extends ComplexByteConverter[DateMidnight] {
    override def write(dm: DateMidnight, output: PrimitiveOutputStream) {
      output.writeLong(dm.getMillis)
    }

    override def read(input: PrimitiveInputStream): DateMidnight = new DateMidnight(input.readLong())


    def apply(year: Int, day: Int): DateMidnight = new DateMidnight().withYear(year).withDayOfYear(day)
  }

  implicit object DateTimeConverter extends ComplexByteConverter[DateTime] {
    override def write(dm: DateTime, output: PrimitiveOutputStream) {
      output.writeLong(dm.getMillis)
    }

    override def read(input: PrimitiveInputStream): DateTime = new DateTime(input.readLong())
  }

  implicit object DateTimeSeqConverter extends SeqConverter[DateTime]

  implicit object DateTimeSetConverter extends SetConverter[DateTime]

  implicit object DateMidnightSeqConverter extends SeqConverter[DateMidnight]

  implicit object DateMidnightSetConverter extends SetConverter[DateMidnight]

  implicit object StringLongMap extends MapConverter[String, Long]
  implicit object ImmutableStringLongMap extends ImmutableMapConverter[String, Long]
  implicit object MutableStringLongMap extends MutableMapConverter[String, Long]

  implicit object StringStringMap extends MapConverter[String,String]
  implicit object MutableStringStringMap extends MutableMapConverter[String,String]
  implicit object ImmutableStringStringMap extends ImmutableMapConverter[String,String]

  /*
  Helper function to make byte arrays out of arbitrary values.
   */
  def makeBytes(writer: (PrimitiveOutputStream) => Unit): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val dataOutput = new PrimitiveOutputStream(bos)
    writer(dataOutput)
    bos.toByteArray
  }

  def makeWritable(writer: (PrimitiveOutputStream) => Unit): BytesWritable = new BytesWritable(makeBytes(writer))

  def readBytes[T](bytes: Array[Byte])(reader: (PrimitiveInputStream) => T): T = {
    val bis = new ByteArrayInputStream(bytes)
    val dis = new PrimitiveInputStream(bis)
    val results = reader(dis)
    dis.close()
    results
  }

  def readWritable[T](bytesWritable: BytesWritable)(reader: (PrimitiveInputStream) => T): T = readBytes(bytesWritable.getBytes)(reader)

}