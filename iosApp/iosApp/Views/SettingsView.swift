import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var appState: AppState
    @State private var notificationsEnabled = true
    @State private var autoSyncEnabled = true
    @State private var showingUserManagementSheet = false

    var body: some View {
        List {
            Section("Perfil do Usuário") {
                HStack(spacing: 16) {
                    Image(systemName: "person.circle.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.blue)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(appState.currentUser?.username.capitalized ?? "Usuário")
                            .font(.headline)
                        Text("Perfil: \(appState.currentUser?.role ?? "ADMIN")")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.vertical, 4)
            }

            Section("Preferências do Sistema") {
                Toggle("Notificações Push (FCM)", isOn: $notificationsEnabled)
                Toggle("Sincronização em Tempo Real", isOn: $autoSyncEnabled)
            }

            if appState.currentUser?.role == "ADMIN" || appState.currentUser?.role == "SÍNDICO" {
                Section("Administração") {
                    Button {
                        showingUserManagementSheet = true
                    } label: {
                        Label("Gestão de Usuários e Permissões", systemImage: "person.2.fill")
                            .foregroundColor(.primary)
                    }
                }
            }

            Section {
                Button(role: .destructive) {
                    appState.logout()
                } label: {
                    HStack {
                        Spacer()
                        Label("Sair da Conta", systemImage: "arrow.right.square")
                            .fontWeight(.bold)
                        Spacer()
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Configurações")
        .sheet(isPresented: $showingUserManagementSheet) {
            UserManagementSheetView()
        }
    }
}

// MARK: - User Management Sheet
struct UserManagementSheetView: View {
    @Environment(\.dismiss) var dismiss

    @State private var users = [
        User(username: "admin", role: "ADMIN"),
        User(username: "sindico", role: "SÍNDICO"),
        User(username: "portaria", role: "PORTARIA"),
        User(username: "morador", role: "MORADOR")
    ]

    var body: some View {
        NavigationStack {
            List {
                ForEach(users) { user in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(user.username)
                                .font(.headline)
                            Text(user.role)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                    }
                }
            }
            .navigationTitle("Usuários do Sistema")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Concluído") { dismiss() }
                }
            }
        }
    }
}
