package com.r3corda.core.serialization

import com.google.common.primitives.Ints
import com.r3corda.core.crypto.generateKeyPair
import com.r3corda.core.crypto.signWithECDSA
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.time.Instant
import java.util.*

class KryoTests {

    private val kryo = createKryo()

    @Test
    fun ok() {
        val birthday = Instant.parse("1984-04-17T00:30:00.00Z")
        val mike = Person("mike", birthday)
        val bits = mike.serialize(kryo)
        assertThat(bits.deserialize(kryo)).isEqualTo(Person("mike", birthday))
    }

    @Test
    fun nullables() {
        val bob = Person("bob", null)
        val bits = bob.serialize(kryo)
        assertThat(bits.deserialize(kryo)).isEqualTo(Person("bob", null))
    }

    @Test
    fun `serialised form is stable when the same object instance is added to the deserialised object graph`() {
        kryo.noReferencesWithin<ArrayList<*>>()
        val obj = Ints.toByteArray(0x01234567).opaque()
        val originalList = arrayListOf(obj)
        val deserialisedList = originalList.serialize(kryo).deserialize(kryo)
        originalList += obj
        deserialisedList += obj
        assertThat(deserialisedList.serialize(kryo)).isEqualTo(originalList.serialize(kryo))
    }

    @Test
    fun `serialised form is stable when the same object instance occurs more than once, and using java serialisation`() {
        kryo.noReferencesWithin<ArrayList<*>>()
        val instant = Instant.ofEpochMilli(123)
        val instantCopy = Instant.ofEpochMilli(123)
        assertThat(instant).isNotSameAs(instantCopy)
        val listWithCopies = arrayListOf(instant, instantCopy)
        val listWithSameInstances = arrayListOf(instant, instant)
        assertThat(listWithSameInstances.serialize(kryo)).isEqualTo(listWithCopies.serialize(kryo))
    }

    @Test
    fun `cyclic object graph`() {
        val cyclic = Cyclic(3)
        val bits = cyclic.serialize(kryo)
        assertThat(bits.deserialize(kryo)).isEqualTo(cyclic)
    }

    @Test
    fun `deserialised keypair functions the same as serialised one`() {
        val keyPair = generateKeyPair()
        val bitsToSign: ByteArray = Ints.toByteArray(0x01234567)
        val wrongBits: ByteArray = Ints.toByteArray(0x76543210)
        val signature = keyPair.signWithECDSA(bitsToSign)
        signature.verifyWithECDSA(bitsToSign)
        assertThatThrownBy { signature.verifyWithECDSA(wrongBits) }

        val deserialisedKeyPair = keyPair.serialize(kryo).deserialize(kryo)
        val deserialisedSignature = deserialisedKeyPair.signWithECDSA(bitsToSign)
        assertThat(deserialisedSignature).isEqualTo(signature)
        deserialisedSignature.verifyWithECDSA(bitsToSign)
        assertThatThrownBy { deserialisedSignature.verifyWithECDSA(wrongBits) }
    }

    private data class Person(val name: String, val birthday: Instant?)

    @Suppress("unused")
    private class Cyclic(val value: Int) {
        val thisInstance = this
        override fun equals(other: Any?): Boolean = (this === other) || (other is Cyclic && this.value == other.value)
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "Cyclic($value)"
    }

}