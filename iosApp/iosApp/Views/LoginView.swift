import SwiftUI

struct LoginView: View {
    @EnvironmentObject var appState: AppState
    @State private var username = ""
    @State private var password = ""
    @State private var selectedRole = "ADMIN"
    @State private var showError = false

    let roles = ["ADMIN", "SÍNDICO", "PORTARIA", "MORADOR"]

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color.blue.opacity(0.8), Color.indigo], startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                // Header Logo / Icon
                VStack(spacing: 12) {
                    Image(systemName: "building.2.crop.circle.fill")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 90, height: 90)
                        .foregroundColor(.white)

                    Text("CondSuites")
                        .font(.system(size: 36, weight: .bold, design: .rounded))
                        .foregroundColor(.white)

                    Text("Gestão Condominial Inteligente")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.85))
                }
                .padding(.bottom, 20)

                // Form Card
                VStack(spacing: 18) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Usuário")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundColor(.gray)

                        HStack {
                            Image(systemName: "person.fill")
                                .foregroundColor(.blue)
                            TextField("Digite seu usuário", text: $username)
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled(true)
                        }
                        .padding()
                        .background(Color.gray.opacity(0.12))
                        .cornerRadius(12)
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Senha")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundColor(.gray)

                        HStack {
                            Image(systemName: "lock.fill")
                                .foregroundColor(.blue)
                            SecureField("Digite sua senha", text: $password)
                        }
                        .padding()
                        .background(Color.gray.opacity(0.12))
                        .cornerRadius(12)
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Perfil de Acesso")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundColor(.gray)

                        Picker("Perfil", selection: $selectedRole) {
                            ForEach(roles, id: \.self) { role in
                                Text(role).tag(role)
                            }
                        }
                        .pickerStyle(.segmented)
                    }

                    if showError {
                        Text("Por favor, preencha todos os campos")
                            .font(.caption)
                            .foregroundColor(.red)
                    }

                    Button(action: handleLogin) {
                        HStack {
                            Spacer()
                            Text("Entrar no CondSuites")
                                .fontWeight(.bold)
                            Spacer()
                        }
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                        .shadow(color: .blue.opacity(0.3), radius: 6, x: 0, y: 4)
                    }
                    .padding(.top, 10)
                }
                .padding(24)
                .background(Color.white)
                .cornerRadius(24)
                .shadow(color: .black.opacity(0.15), radius: 15, x: 0, y: 10)
                .padding(.horizontal, 24)

                Spacer()

                Text("v1.0.0 • iOS CondSuites Edition")
                    .font(.footnote)
                    .foregroundColor(.white.opacity(0.7))
                    .padding(.bottom, 10)
            }
        }
    }

    private func handleLogin() {
        let userToLogIn = username.isEmpty ? "admin" : username
        appState.login(username: userToLogIn, role: selectedRole)
    }
}
