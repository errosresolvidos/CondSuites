const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// Carregue suas credenciais de serviço do Firebase
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

/**
 * Endpoint para registrar o token FCM do dispositivo Android no Firestore
 * (Garante persistência mesmo quando o servidor no Render reinicia ou dorme)
 */
app.post('/register-token', async (req, res) => {
  try {
    const { token } = req.body;
    if (!token) {
      return res.status(400).json({ error: 'Token FCM é obrigatório.' });
    }

    // Salva ou atualiza o token na coleção 'fcm_tokens' do Firestore
    await db.collection('fcm_tokens').doc(token).set({
      token: token,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    console.log(`Token FCM salvo/atualizado no Firestore: ${token}`);
    return res.status(200).json({ success: true, message: 'Token registrado com sucesso.' });
  } catch (error) {
    console.error('Erro ao registrar token no Firestore:', error);
    return res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint para envio de notificações Push usando a API v1 do Firebase FCM.
 * Busca os tokens salvos no Firestore e realiza o envio em Multicast (sendEachForMulticast).
 */
app.post('/send-notification', async (req, res) => {
  try {
    const { token, title, body, senderUsername, occurrenceId } = req.body;

    const baseMessage = {
      notification: {
        title: String(title || 'CondSuites'),
        body: String(body || 'Nova notificação do condomínio.')
      },
      data: {
        title: String(title || 'CondSuites'),
        body: String(body || 'Nova notificação do condomínio.'),
        ...(senderUsername && { senderUsername: String(senderUsername) }),
        ...(occurrenceId && { occurrenceId: String(occurrenceId) })
      },
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          channelId: 'condsuites_notifications_channel',
          defaultSound: true,
          defaultVibrateTimings: true
        }
      }
    };

    if (token) {
      // Envio direcionado a um único token
      const response = await admin.messaging().send({ ...baseMessage, token });
      console.log('Notificação enviada para token específico:', response);
      return res.status(200).json({ success: true, messageId: response });
    } else {
      // Busca todos os tokens cadastrados no Firestore
      const snapshot = await db.collection('fcm_tokens').get();
      const tokensArray = [];
      snapshot.forEach(doc => {
        if (doc.id) {
          tokensArray.push(doc.id);
        }
      });

      console.log(`Tokens encontrados no Firestore para disparo: ${tokensArray.length}`);

      if (tokensArray.length > 0) {
        // Envio em multicast para todos os tokens do Firestore
        const multicastMessage = {
          ...baseMessage,
          tokens: tokensArray
        };
        const response = await admin.messaging().sendEachForMulticast(multicastMessage);
        console.log(`Notificações Multicast enviadas: ${response.successCount} sucessos, ${response.failureCount} falhas.`);
        return res.status(200).json({
          success: true,
          successCount: response.successCount,
          failureCount: response.failureCount
        });
      } else {
        // Fallback para tópico caso a coleção esteja vazia
        console.log('Nenhum token no Firestore, enviando para tópico occurrences');
        const response = await admin.messaging().send({ ...baseMessage, topic: 'occurrences' });
        return res.status(200).json({ success: true, messageId: response });
      }
    }

  } catch (error) {
    console.error('Erro ao enviar notificação FCM v1:', error);
    return res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor Node.js rodando na porta ${PORT}`);
});
