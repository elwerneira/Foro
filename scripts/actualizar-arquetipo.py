from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
pom_path = root / 'pom.xml'
pom_tree = ET.parse(pom_path).getroot()
group = pom_tree.find('m:groupId', ns).text
package = group + '.service'
component = 'usuarios' if group == 'usuario' else 'foro'
dest = root / 'arquetipo/src/main/resources/archetype-resources'
files = []
# Lista permitida: las wallets y la configuración privada nunca forman parte de la plantilla.
for folder in ['src/main/java', 'src/test/java', 'src/main/resources', 'src/test/resources', 'sql']:
    files.extend(p for p in (root / folder).rglob('*') if p.is_file())
files.extend(root / p for p in ['mvnw', 'mvnw.cmd', '.mvn/wrapper/maven-wrapper.properties',
    '.gitignore', '.gitattributes', '.dockerignore', 'Dockerfile', 'config/application-local.example.properties'])
for source in files:
    relative = source.relative_to(root)
    data = source.read_text(encoding='utf-8-sig')
    if source.suffix == '.java':
        base = 'src/main/java' if relative.as_posix().startswith('src/main/java/') else 'src/test/java'
        relative = Path(base) / source.relative_to(root / base / package.replace('.', '/'))
        data = data.replace(package, '${package}')
    if source.name == 'Dockerfile':
        data = data.replace('service-0.0.1-SNAPSHOT.jar', '${artifactId}-${version}.jar')
    target = dest / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(data, encoding='utf-8', newline='\n')
pom = pom_path.read_text(encoding='utf-8-sig')
pom = pom.replace('<groupId>' + group + '</groupId>', '<groupId>${groupId}</groupId>', 1)
pom = pom.replace('<artifactId>service</artifactId>', '<artifactId>${artifactId}</artifactId>', 1)
pom = pom.replace('<version>0.0.1-SNAPSHOT</version>', '<version>${version}</version>', 1)
pom = pom.replace('${java.version}', '${symbol_dollar}{java.version}')
(dest / 'pom.xml').write_text("#set( $symbol_dollar = '$' )\n" + pom, encoding='utf-8', newline='\n')
assert not any('wallet' in str(p.relative_to(dest)).lower() or p.name == 'application-local.properties' for p in dest.rglob('*'))
assert '${package}' in next((dest / 'src/main/java/model').glob('*.java')).read_text(encoding='utf-8')
print('Arquetipo ' + component + ': ' + str(len(files)+1) + ' archivos públicos preparados.')
