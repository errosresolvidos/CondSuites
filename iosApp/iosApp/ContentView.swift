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
                    Label("Início", systemName: "house.fill")
                }

            UnitsView()
                .tabItem {
                    Label("Unidades", systemName: "building.2.fill")
                }

            OccurrencesView()
                .tabItem {
                    Label("Ocorrências", systemName: "exclamationmark.triangle.fill")
                }

            FinanceView()
                .tabItem {
                    Label("Financeiro", systemName: "dollarsign.circle.fill")
                }

            MoreMenuView()
                .tabItem {
                    Label("Mais", systemName: "ellipsis.circle.fill")
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
                        Label("Acordos de Cobrança", systemName: "handshake.fill")
                    }

                    NavigationLink(destination: LawsuitsView()) {
                        Label("Ações Judiciais (Ajuizados)", systemName: "gavel")
                    }

                    NavigationLink(destination: ElevatorsView()) {
                        Label("Gestão de Elevadores", systemName: "arrow.up.and.down.square.fill")
                    }

                    NavigationLink(destination: ContractsView()) {
                        Label("Contratos de Terceiros", systemName: "doc.text.fill")
                    }

                    NavigationLink(destination: OvertimeView()) {
                        Label("Lançar Horas Extras", systemName: "clock.badge.plus")
                    }
                }

                Section("Análises & Relatórios") {
                    NavigationLink(destination: ReportsView()) {
                        Label("Relatórios Gerenciais", systemName: "chart.bar.doc.horizontal.fill")
                    }
                }

                Section("Ajustes") {
                    NavigationLink(destination: SettingsView()) {
                        Label("Configurações", systemName: "gearshape.fill")
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Mais Opções")
        }
    }
}
