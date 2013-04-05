/*
 * Threading.scala
 * (ScissDSP)
 *
 * Copyright (c) 2001-2013 Hanns Holger Rutz. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.dsp

import edu.emory.mathcs.utils.ConcurrencyUtils
import de.sciss.serial.{DataOutput, DataInput, ImmutableSerializer}
import scala.annotation.switch

object Threading {
  private final val COOKIE    = 0x5468
  private final val ID_MULTI  = 0
  private final val ID_SINGLE = 1
  private final val ID_CUSTOM = 2

  implicit object Serializer extends ImmutableSerializer[Threading] {
    // don't worry about the exhaustiveness warning. seems to be SI-7298, to be fixed in Scala 2.10.2
    def write(v: Threading, out: DataOutput) {
      out.writeShort(COOKIE)
      v match  {
        case Multi      => out.writeByte(ID_MULTI)
        case Single     => out.writeByte(ID_SINGLE)
        case Custom(n)  => out.writeByte(ID_CUSTOM); out.writeShort(n)
      }
    }

    def read(in: DataInput): Threading = {
      val cookie = in.readShort()
      require(cookie == COOKIE, s"Unexpected cookie $cookie")
      (in.readByte(): @switch) match {
        case ID_MULTI   => Multi
        case ID_SINGLE  => Single
        case ID_CUSTOM  => val n = in.readShort(); Custom(n)
      }
    }
  }

  /**
   * Use the optimal number of threads (equal to the number of cores reported for the CPU)
   */
  case object Multi extends Threading {
    private[dsp] def setJTransforms() {
      ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.getNumberOfProcessors)
    }
  }

  /**
   * Use only single threaded processing
   */
  case object Single extends Threading {
    private[dsp] def setJTransforms() {
      ConcurrencyUtils.setNumberOfThreads(1)
    }
  }

  /**
   * Use a custom number of threads.
   */
  final case class Custom(numThreads: Int) extends Threading {
    private[dsp] def setJTransforms() {
      ConcurrencyUtils.setNumberOfThreads(numThreads)
    }
  }

}

sealed trait Threading {
  private[dsp] def setJTransforms(): Unit
}
