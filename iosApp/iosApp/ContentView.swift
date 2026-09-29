import SwiftUI

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        Group {
            if appState.isAuthenticated && appState.currentUser != nil {
                MainTabView()
            } else {
                LoginView()
            }
        }
    }
}

// MARK: - Main Tab Bar View for iPhone
struct MainTabView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Início", systemImage: "house.fill")
                }

            NavigationStack {
                UnitsView()
            }
            .tabItem {
                Label("Unidades", systemImage: "building.2.fill")
            }

            NavigationStack {
                OccurrencesView()
            }
            .tabItem {
                Label("Ocorrências", systemImage: "exclamationmark.triangle.fill")
            }

            NavigationStack {
                FinanceView()
            }
            .tabItem {
                Label("Financeiro", systemImage: "dollarsign.circle.fill")
            }

            MoreMenuView()
                .tabItem {
                    Label("Mais", systemImage: "ellipsis.circle.fill")
                }
        }
    }
}

// MARK: - More Menu Navigation View
struct MoreMenuView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Gestão & Operação") {
                    NavigationLink(destination: AgreementsView()) {
                        Label("Acordos de Cobrança", systemImage: "handshake.fill")
                    }

                    NavigationLink(destination: LawsuitsView()) {
                        Label("Ações Judiciais (Ajuizados)", systemImage: "gavel")
                    }

                    NavigationLink(destination: ElevatorsView()) {
                        Label("Gestão de Elevadores", systemImage: "arrow.up.and.down.square.fill")
                    }

                    NavigationLink(destination: ContractsView()) {
                        Label("Contratos de Terceiros", systemImage: "doc.text.fill")
                    }

                    NavigationLink(destination: OvertimeView()) {
                        Label("Lançar Horas Extras", systemImage: "clock.badge.plus")
                    }
                }

                Section("Análises & Relatórios") {
                    NavigationLink(destination: ReportsView()) {
                        Label("Relatórios Gerenciais", systemImage: "chart.bar.doc.horizontal.fill")
                    }
                }

                Section("Ajustes") {
                    NavigationLink(destination: SettingsView()) {
                        Label("Configurações", systemImage: "gearshape.fill")
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Mais Opções")
        }
    }
}
