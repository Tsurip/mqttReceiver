package com.tsurip.mqttreceiver

import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

/**     MQTTCONFIGURE CLASS
 *
 *      Essa Classe foi feita para configuração do uso do MQTT, sendo necessário algumas aplicações
 *  ao MainActivity ou a Activity em que será desenvolvida.
 *
 *  PRIMEIRO
 *
 *      Insira o repositório da Eclipse Paho no seu Gadle Project
 *
 *  allprojects {
 *      repositories {
 *          google()
 *          jcenter()
 *          maven {
 *              url "https://repo.eclipse.org/content/repositories/paho-releases/"
 *          }
 *      }
 *  }
 *
 * SEGUNDO
 *
 *      Insira o repositório da Eclipse Paho no seu Gadle Module
 *
 *  dependencies {
 *      implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0'
 *      implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
 *  }
 *
 *  TERCEIRO
 *
 *      Insira no manifests as permissões
 *
 *      <uses-permission android:name="android.permission.WAKE_LOCK" />
 *      <uses-permission android:name="android.permission.INTERNET" />
 *      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *      <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 *
 *      E também insira dentro de <Application/>, porém fora de <Activity/>
 *
 *      <service android:name="org.eclipse.paho.android.service.MqttService">
 *      </service>
 *
 *  QUARTO
 *
 *      Insira a função Conectar a função princípal. Ela está comentada e é a ultima função.
 *
 *  QUINTO
 *
 *      Insira um objeto com lateinit no início da Activity para que ela seja disponível para toda a
 *  Activity. A mesma será configurada na função Conectar.
 *
 *  SEXTO
 *
 *      É necessário um receptor para inserir os dados a função. Para isso, uso editText ou insira o
 *  valor de modo direto caso não vá usar os valores aleatórios.
 *
 *  REFERÊNCIAS USADAS
 *
 *  https://www.youtube.com/watch?v=NpURY3zE8o8
 *  https://github.com/anoop4real/KotlinMQTTSample
 *  https://github.com/eclipse/paho.mqtt.android
 *  https://medium.com/@chaitanya.bhojwani1012/eclipse-paho-mqtt-android-client-using-kotlin-56129ff5fbe7
 *  https://github.com/thebehera/mqtt
 */


class MQTTConfigure(val context: Context,val Parametros: MQTTConnectionParams){


    val client = MqttAndroidClient(context,Parametros.Host, MqttClient.generateClientId())
/*
    Conectar
        Função de connect para se conectar ao broker.
 */

    fun connect(v: TextView?){
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        //mqttConnectOptions.setUserName(this.connectionParams.username)
        //mqttConnectOptions.setPassword(this.connectionParams.password.toCharArray())

        try
        {
            val params = this.Parametros
            client.connect(mqttConnectOptions, null, object: IMqttActionListener {
                override fun onSuccess(asyncActionToken:IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    client.setBufferOpts(disconnectedBufferOptions)
                    Toast.makeText(context,"Conectado ao Server", Toast.LENGTH_SHORT).show()
                    v?.setBackgroundColor(ContextCompat.getColor(context, R.color.Verde))
                    v?.text = "Conectado"
                }
                override fun onFailure(asyncActionToken:IMqttToken, exception:Throwable) {
                    Toast.makeText(context,"Falhou em conectar em: " + Parametros.Host + exception.toString(), Toast.LENGTH_SHORT).show()
                }
            })
        }
        catch (ex:MqttException) {
            ex.printStackTrace()
        }
    }

    /*
    Publisher
        O Publisher é responsável por enviar a mensagem ao MqttBroker. Ele recebe uma var do tipo st
ring e utiliza a uma function do AndroidClient. Ela recebe os valores do Topic, o nível de qos, se a
mensagem é retida, o userContext e aciona o Object ActionListener, que gera duas funções de sucesso
ou
     */
    fun publish(message:String){
        try
        {
            var msg = "Android diz: $message"
            client.publish(this.Parametros.topic,msg.toByteArray(),2,true,this,object :IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.w("Mqtt", "Sucesso em Publish!")
                    Toast.makeText(context,"Foi publicado ao Topic", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("Mqtt", "Publish Failed!")
                    Toast.makeText(context,"Falhou em publicar ao Topic", Toast.LENGTH_SHORT).show()
                }

            })
        }
        catch (ex:MqttException) {
            Toast.makeText(context,"Publish Exception", Toast.LENGTH_SHORT).show()
            ex.printStackTrace()
        }
    }

    /*
    Subscribe
        É a função que determina o Topic no qual o cliente irá fazer subscribe. Ele recebe uma var
do tipo string, que é o Topic no qual será feito o Subcribe. Para isso ele utiliza uma função do IMq
ttAndroidClient.
    */
    fun subscribe(topic: String){
        try
        {
            client.subscribe(topic, 0, null, object:IMqttActionListener {
                override fun onSuccess(asyncActionToken:IMqttToken) {
                    Toast.makeText(context,"Se increveu no Topic", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(asyncActionToken:IMqttToken, exception:Throwable) {
                    Toast.makeText(context,"Falhou em se increver no Topic", Toast.LENGTH_SHORT).show()

                }
            })
        }
        catch (ex:MqttException) {
            Toast.makeText(context,"Exception Subscribing", Toast.LENGTH_SHORT).show()
            ex.printStackTrace()
        }
    }

    /*
    Receiver
        Ele é responsável por receber as mensagens e aplicá-las da forma que for. Para alterar a fun
ção do mesmo, insira uma nova codagem function messageArrived, que é a função que recepciona as mens
agens.

     */

    fun receiveMessages(messageActiviter: TextView?) {
        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {}
            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    var newText = """
                    --->${message.toString()}
                    """
                    //var newText = text.toString() + "\n" + message +  "\n"
                    messageActiviter?.text = newText
                } catch (e: Exception) {
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })
    }
}

class MQTTConnectionParams (val clientId: String?,val Host: String, val topic: String, val username: String?, val password: String?){}

/* Colocar como function da MainActivity e dar call para conectar ao broker

    fun connect(v: TextView?){
    // Essa TextView v é a textView da statusBar. A mesma é nullsafe, então caso não tenha, insira null quando utilizar a função

        if (!(edHostServer.text.isNullOrEmpty() && edTopicPublish.text.isNullOrEmpty())) {
            var host = "tcp://" + edHostServer.text.toString() + ":1883"
            var topic = edTopicPublish.text.toString()
            var configParams = MQTTConnectionParams(null, host, topic, null, null)

            client = MQTTConfigure(this, configParams)
            client.connect(v)
        }else{
            Toast.makeText(this, "É nn deu nn em", Toast.LENGTH_SHORT).show()
        }

    }
 */