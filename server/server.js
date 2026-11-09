const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// Carregue suas credenciais de serviço do Firebase
// Certifique-se de colocar o arquivo serviceAccountKey.json na mesma pasta server/
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

/**
 * Endpoint para enviar notificação push para um dispositivo específico (FCM v1)
 */
app.post('/send-notification', async (req, res) => {
  try {
    const { token, title, body, senderUsername, occurrenceId } = req.body;

    if (!token) {
      return res.status(400).json({ error: 'O token FCM do dispositivo é obrigatório.' });
    }

    const message = {
      token: token,
      notification: {
        title: title || 'CondSuites',
        body: body || 'Nova notificação do condomínio.'
      },
      data: {
        title: title || 'CondSuites',
        body: body || 'Nova notificação do condomínio.',
        ...(senderUsername && { senderUsername }),
        ...(occurrenceId && { occurrenceId: String(occurrenceId) })
      },
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          channelId: 'default'
        }
      }
    };

    const response = await admin.messaging().send(message);

    console.log('Notificação enviada com sucesso:', response);
    return res.status(200).json({
      success: true,
      messageId: response
    });

  } catch (error) {
    console.error('Erro ao enviar notificação:', error);
    return res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});
